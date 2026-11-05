package com.shoesmarket.infra.persistence

import com.shoesmarket.domain.InvalidStatusTransitionException
import com.shoesmarket.domain.PaymentStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "payment")
class PaymentEntity(
    reservationId: Long,
    idempotencyKey: String,
    pgOrderId: String,
    amount: Long,
    status: PaymentStatus = PaymentStatus.READY,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(name = "order_id")
    var orderId: Long? = null
        protected set

    @Column(name = "reservation_id", nullable = false)
    var reservationId: Long = reservationId
        protected set

    @Column(name = "idempotency_key", nullable = false, length = 100)
    var idempotencyKey: String = idempotencyKey
        protected set

    @Column(name = "pg_payment_key", length = 200)
    var pgPaymentKey: String? = null
        protected set

    @Column(name = "pg_order_id", nullable = false, length = 100)
    var pgOrderId: String = pgOrderId
        protected set

    @Column(name = "amount", nullable = false)
    var amount: Long = amount
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: PaymentStatus = status
        protected set

    @Column(name = "approved_at")
    var approvedAt: Instant? = null
        protected set

    @Column(name = "failure_reason", length = 500)
    var failureReason: String? = null
        protected set

    private fun transitTo(next: PaymentStatus) {
        if (!status.canTransitTo(next)) {
            throw InvalidStatusTransitionException("결제 상태 전이 불가: $status -> $next")
        }
        status = next
    }

    fun approve(
        orderId: Long,
        pgPaymentKey: String,
        approvedAt: Instant,
    ) {
        transitTo(PaymentStatus.APPROVED)
        this.orderId = orderId
        this.pgPaymentKey = pgPaymentKey
        this.approvedAt = approvedAt
    }

    fun fail(reason: String) {
        transitTo(PaymentStatus.FAILED)
        this.failureReason = reason
    }

    fun cancel() = transitTo(PaymentStatus.CANCELLED)
}
