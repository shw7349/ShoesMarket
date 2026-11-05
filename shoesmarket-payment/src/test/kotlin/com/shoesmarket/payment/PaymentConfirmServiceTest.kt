package com.shoesmarket.payment

import com.shoesmarket.domain.PaymentConfirmResult
import com.shoesmarket.infra.persistence.DropEventEntity
import com.shoesmarket.infra.persistence.DropEventRepository
import com.shoesmarket.infra.persistence.OrderRepository
import com.shoesmarket.infra.persistence.PaymentEntity
import com.shoesmarket.infra.persistence.PaymentRepository
import com.shoesmarket.infra.persistence.ProductEntity
import com.shoesmarket.infra.persistence.ProductRepository
import com.shoesmarket.infra.persistence.ReservationEntity
import com.shoesmarket.infra.persistence.ReservationRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Optional

class PaymentConfirmServiceTest :
    DescribeSpec({
        val paymentRepository = mockk<PaymentRepository>(relaxed = true)
        val reservationRepository = mockk<ReservationRepository>()
        val orderRepository = mockk<OrderRepository>()
        val dropEventRepository = mockk<DropEventRepository>()
        val productRepository = mockk<ProductRepository>()
        val tossClient = mockk<TossPaymentClient>(relaxed = true)
        val clock = Clock.fixed(Instant.parse("2026-11-20T11:00:00Z"), ZoneOffset.UTC)
        val service =
            PaymentConfirmService(
                paymentRepository,
                reservationRepository,
                orderRepository,
                dropEventRepository,
                productRepository,
                tossClient,
                clock,
            )

        val command = PaymentConfirmCommand(paymentKey = "pk_1", pgOrderId = "ord_1", amount = 139_000, reservationId = 1)

        fun reservation(expiresAt: Instant) = ReservationEntity(dropId = 10, userId = 42, expiresAt = expiresAt)

        beforeEach {
            clearMocks(
                paymentRepository,
                reservationRepository,
                orderRepository,
                dropEventRepository,
                productRepository,
                tossClient,
            )
            every { paymentRepository.findByIdempotencyKey(any()) } returns null
            every { paymentRepository.save(any()) } answers { firstArg() }
            every { reservationRepository.findById(1) } returns Optional.of(reservation(Instant.parse("2026-11-20T11:05:00Z")))
            every { dropEventRepository.findById(10) } returns
                Optional.of(DropEventEntity(1, 500, 1, Instant.parse("2026-11-20T11:00:00Z"), Instant.parse("2026-11-20T12:00:00Z")))
            every { productRepository.findById(1) } returns Optional.of(ProductEntity("에어포스1", "나이키", 139_000))
            every { reservationRepository.confirmIfReserved(1) } returns 1
            every { orderRepository.save(any()) } answers { firstArg() }
            every { tossClient.approve(any(), any(), any()) } returns TossApproval("pk_1", "ord_1", 139_000, "DONE")
        }

        describe("결제 승인") {
            it("정상 승인하면 주문을 생성하고 PG를 1회 호출한다") {
                val result = service.confirm(command)

                result.shouldBeInstanceOf<PaymentConfirmResult.Success>()
                verify(exactly = 1) { tossClient.approve("pk_1", "ord_1", 139_000) }
            }

            it("이미 승인된 결제(멱등 재요청)는 PG를 호출하지 않고 기존 주문을 반환한다") {
                val approved =
                    PaymentEntity(reservationId = 1, idempotencyKey = "reservation:1", pgOrderId = "ord_1", amount = 139_000)
                approved.approve(orderId = 5, pgPaymentKey = "pk_1", approvedAt = clock.instant())
                every { paymentRepository.findByIdempotencyKey("reservation:1") } returns approved

                val result = service.confirm(command)

                result.shouldBeInstanceOf<PaymentConfirmResult.AlreadyConfirmed>()
                verify(exactly = 0) { tossClient.approve(any(), any(), any()) }
            }

            it("금액이 다르면 AmountMismatch") {
                service.confirm(command.copy(amount = 100_000)) shouldBe PaymentConfirmResult.AmountMismatch
                verify(exactly = 0) { tossClient.approve(any(), any(), any()) }
            }

            it("만료된 선점이면 ReservationExpired") {
                every { reservationRepository.findById(1) } returns Optional.of(reservation(Instant.parse("2026-11-20T10:00:00Z")))

                service.confirm(command) shouldBe PaymentConfirmResult.ReservationExpired
            }

            it("확정 직전 만료와 경합하면(영향 행 0) PG를 취소하고 ReservationExpired") {
                every { reservationRepository.confirmIfReserved(1) } returns 0

                val result = service.confirm(command)

                result shouldBe PaymentConfirmResult.ReservationExpired
                verify(exactly = 1) { tossClient.cancel("pk_1", any()) }
            }
        }
    })
