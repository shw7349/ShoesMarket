package com.shoesmarket.payment

import com.shoesmarket.domain.OrderId
import com.shoesmarket.domain.PaymentConfirmResult
import com.shoesmarket.domain.ReservationStatus
import com.shoesmarket.infra.persistence.DropEventRepository
import com.shoesmarket.infra.persistence.OrderEntity
import com.shoesmarket.infra.persistence.OrderRepository
import com.shoesmarket.infra.persistence.PaymentEntity
import com.shoesmarket.infra.persistence.PaymentRepository
import com.shoesmarket.infra.persistence.ProductRepository
import com.shoesmarket.infra.persistence.ReservationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

data class PaymentConfirmCommand(
    val paymentKey: String,
    val pgOrderId: String,
    val amount: Long,
    val reservationId: Long,
)

/**
 * 결제 승인 (명세 7.2). 멱등성 키로 중복 승인을 막고, 만료와 경합하면 조건부 UPDATE로 한쪽만 성공한다.
 */
@Service
class PaymentConfirmService(
    private val paymentRepository: PaymentRepository,
    private val reservationRepository: ReservationRepository,
    private val orderRepository: OrderRepository,
    private val dropEventRepository: DropEventRepository,
    private val productRepository: ProductRepository,
    private val tossClient: TossPaymentClient,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun confirm(command: PaymentConfirmCommand): PaymentConfirmResult {
        val idempotencyKey = "reservation:${command.reservationId}"

        val existing = paymentRepository.findByIdempotencyKey(idempotencyKey)
        if (existing != null && existing.status == com.shoesmarket.domain.PaymentStatus.APPROVED) {
            // 멱등: 이미 승인된 결제 — PG를 다시 호출하지 않고 기존 주문을 반환한다.
            val orderId = existing.orderId ?: orderRepository.findByReservationId(command.reservationId)?.id
            return PaymentConfirmResult.AlreadyConfirmed(OrderId(orderId ?: 0))
        }

        val reservation =
            reservationRepository.findById(command.reservationId).orElse(null)
                ?: return PaymentConfirmResult.ReservationNotFound
        if (reservation.status != ReservationStatus.RESERVED || !reservation.expiresAt.isAfter(clock.instant())) {
            return PaymentConfirmResult.ReservationExpired
        }

        val drop =
            dropEventRepository.findById(reservation.dropId).orElse(null)
                ?: return PaymentConfirmResult.ReservationNotFound
        val product =
            productRepository.findById(drop.productId).orElse(null)
                ?: return PaymentConfirmResult.ReservationNotFound
        if (product.price != command.amount) {
            return PaymentConfirmResult.AmountMismatch
        }

        val payment =
            existing ?: paymentRepository.save(
                PaymentEntity(
                    reservationId = command.reservationId,
                    idempotencyKey = idempotencyKey,
                    pgOrderId = command.pgOrderId,
                    amount = command.amount,
                ),
            )

        val approval =
            try {
                tossClient.approve(command.paymentKey, command.pgOrderId, command.amount)
            } catch (e: TossPaymentException) {
                payment.fail(e.message ?: "결제 승인 실패")
                paymentRepository.save(payment)
                return PaymentConfirmResult.PaymentFailed
            }

        // 조건부 확정: 만료 스케줄러와 경합하면 영향 행 0 → PG 취소 후 만료 응답
        if (reservationRepository.confirmIfReserved(command.reservationId) == 0) {
            log.warn("선점 만료와 경합 — 결제 취소 reservationId={}", command.reservationId)
            runCatching { tossClient.cancel(command.paymentKey, "선점 만료와 경합") }
            payment.fail("선점 만료와 경합으로 취소")
            paymentRepository.save(payment)
            return PaymentConfirmResult.ReservationExpired
        }

        val order =
            orderRepository.save(
                OrderEntity(
                    reservationId = command.reservationId,
                    userId = reservation.userId,
                    dropId = reservation.dropId,
                    amount = command.amount,
                ),
            )
        payment.approve(order.id, approval.paymentKey, clock.instant())
        paymentRepository.save(payment)
        return PaymentConfirmResult.Success(OrderId(order.id))
    }
}
