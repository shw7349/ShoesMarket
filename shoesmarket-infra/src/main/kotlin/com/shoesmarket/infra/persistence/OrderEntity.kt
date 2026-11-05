package com.shoesmarket.infra.persistence

import com.shoesmarket.domain.InvalidStatusTransitionException
import com.shoesmarket.domain.OrderStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "orders")
class OrderEntity(
    reservationId: Long,
    userId: Long,
    dropId: Long,
    amount: Long,
    status: OrderStatus = OrderStatus.PAID,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(name = "reservation_id", nullable = false)
    var reservationId: Long = reservationId
        protected set

    @Column(name = "user_id", nullable = false)
    var userId: Long = userId
        protected set

    @Column(name = "drop_id", nullable = false)
    var dropId: Long = dropId
        protected set

    @Column(name = "amount", nullable = false)
    var amount: Long = amount
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: OrderStatus = status
        protected set

    fun cancel() {
        if (!status.canTransitTo(OrderStatus.CANCELLED)) {
            throw InvalidStatusTransitionException("주문 상태 전이 불가: $status -> ${OrderStatus.CANCELLED}")
        }
        status = OrderStatus.CANCELLED
    }
}
