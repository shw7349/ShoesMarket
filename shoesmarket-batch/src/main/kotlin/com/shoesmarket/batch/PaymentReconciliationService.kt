package com.shoesmarket.batch

import com.shoesmarket.domain.PaymentStatus
import com.shoesmarket.infra.persistence.OrderEntity
import com.shoesmarket.infra.persistence.OrderRepository
import com.shoesmarket.infra.persistence.PaymentRepository
import com.shoesmarket.infra.persistence.ReservationRepository
import com.shoesmarket.payment.TossPaymentClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

data class ReconcileSummary(
    val scanned: Int,
    val recovered: Int,
    val failed: Int,
)

/**
 * 결제 대사 (명세 8장). READY로 일정 시간 멈춘 결제를 PG 조회해서,
 * PG 승인된 건은 주문을 복구하고(서버 다운 등으로 DB 반영 실패한 경우), 미승인 건은 FAILED 처리한다.
 */
@Service
class PaymentReconciliationService(
    private val paymentRepository: PaymentRepository,
    private val reservationRepository: ReservationRepository,
    private val orderRepository: OrderRepository,
    private val tossClient: TossPaymentClient,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun reconcile(olderThanMinutes: Long = 5): ReconcileSummary {
        val threshold = clock.instant().minusSeconds(olderThanMinutes * 60)
        val stuck = paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.READY, threshold)
        var recovered = 0
        var failed = 0

        stuck.forEach { payment ->
            val approval = runCatching { tossClient.getByOrderId(payment.pgOrderId) }.getOrNull()
            when {
                approval != null && approval.status == "DONE" -> {
                    if (reservationRepository.confirmIfReserved(payment.reservationId) == 1) {
                        val reservation = reservationRepository.findById(payment.reservationId).orElse(null)
                        if (reservation != null) {
                            val order =
                                orderRepository.save(
                                    OrderEntity(
                                        reservationId = payment.reservationId,
                                        userId = reservation.userId,
                                        dropId = reservation.dropId,
                                        amount = payment.amount,
                                    ),
                                )
                            payment.approve(order.id, approval.paymentKey, clock.instant())
                            paymentRepository.save(payment)
                            recovered++
                            log.info("대사 복구: 결제 승인 반영 reservationId={}", payment.reservationId)
                        }
                    } else {
                        runCatching { tossClient.cancel(approval.paymentKey, "대사: 선점 확정 불가") }
                        payment.fail("대사: 선점 확정 불가로 취소")
                        paymentRepository.save(payment)
                        failed++
                    }
                }

                else -> {
                    payment.fail("대사: PG 미승인")
                    paymentRepository.save(payment)
                    failed++
                }
            }
        }
        return ReconcileSummary(stuck.size, recovered, failed)
    }
}
