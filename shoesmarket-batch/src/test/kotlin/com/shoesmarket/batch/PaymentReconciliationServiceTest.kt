package com.shoesmarket.batch

import com.shoesmarket.domain.PaymentStatus
import com.shoesmarket.infra.persistence.OrderRepository
import com.shoesmarket.infra.persistence.PaymentEntity
import com.shoesmarket.infra.persistence.PaymentRepository
import com.shoesmarket.infra.persistence.ReservationEntity
import com.shoesmarket.infra.persistence.ReservationRepository
import com.shoesmarket.payment.TossApproval
import com.shoesmarket.payment.TossPaymentClient
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Optional

/**
 * 장애 복구 시나리오: PG 승인은 됐는데 DB 반영이 실패해 READY로 멈춘 결제를 대사 배치가 복구한다 (명세 9.2).
 */
class PaymentReconciliationServiceTest :
    DescribeSpec({
        val paymentRepository = mockk<PaymentRepository>(relaxed = true)
        val reservationRepository = mockk<ReservationRepository>(relaxed = true)
        val orderRepository = mockk<OrderRepository>()
        val tossClient = mockk<TossPaymentClient>(relaxed = true)
        val clock = Clock.fixed(Instant.parse("2026-11-20T12:00:00Z"), ZoneOffset.UTC)
        val service = PaymentReconciliationService(paymentRepository, reservationRepository, orderRepository, tossClient, clock)

        fun stuckPayment() = PaymentEntity(reservationId = 1, idempotencyKey = "reservation:1", pgOrderId = "ord_1", amount = 139_000)

        beforeEach {
            clearMocks(paymentRepository, reservationRepository, orderRepository, tossClient)
            every { paymentRepository.save(any()) } answers { firstArg() }
            every { orderRepository.save(any()) } answers { firstArg() }
            every { reservationRepository.findById(1) } returns
                Optional.of(ReservationEntity(dropId = 10, userId = 42, expiresAt = Instant.parse("2026-11-20T11:00:00Z")))
        }

        describe("결제 대사") {
            it("PG 승인됐지만 DB 미반영이면 주문을 복구한다") {
                every { paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.READY, any()) } returns listOf(stuckPayment())
                every { tossClient.getByOrderId("ord_1") } returns TossApproval("pk_1", "ord_1", 139_000, "DONE")
                every { reservationRepository.confirmIfReserved(1) } returns 1

                val summary = service.reconcile()

                summary.recovered shouldBe 1
                summary.failed shouldBe 0
                verify(exactly = 1) { orderRepository.save(any()) }
            }

            it("PG 미승인이면 FAILED 처리한다") {
                every { paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.READY, any()) } returns listOf(stuckPayment())
                every { tossClient.getByOrderId("ord_1") } returns null

                val summary = service.reconcile()

                summary.recovered shouldBe 0
                summary.failed shouldBe 1
                verify(exactly = 0) { orderRepository.save(any()) }
            }

            it("PG 승인됐지만 선점 확정 불가(만료됨)면 PG를 취소하고 FAILED 처리한다") {
                every { paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.READY, any()) } returns listOf(stuckPayment())
                every { tossClient.getByOrderId("ord_1") } returns TossApproval("pk_1", "ord_1", 139_000, "DONE")
                every { reservationRepository.confirmIfReserved(1) } returns 0

                val summary = service.reconcile()

                summary.failed shouldBe 1
                verify(exactly = 1) { tossClient.cancel("pk_1", any()) }
            }
        }
    })
