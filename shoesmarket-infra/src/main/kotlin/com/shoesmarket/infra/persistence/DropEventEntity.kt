package com.shoesmarket.infra.persistence

import com.shoesmarket.domain.DropStatus
import com.shoesmarket.domain.InvalidStatusTransitionException
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
@Table(name = "drop_event")
class DropEventEntity(
    productId: Long,
    totalStock: Int,
    perUserLimit: Int,
    openAt: Instant,
    closeAt: Instant,
    status: DropStatus = DropStatus.SCHEDULED,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(name = "product_id", nullable = false)
    var productId: Long = productId
        protected set

    @Column(name = "total_stock", nullable = false)
    var totalStock: Int = totalStock
        protected set

    @Column(name = "per_user_limit", nullable = false)
    var perUserLimit: Int = perUserLimit
        protected set

    @Column(name = "open_at", nullable = false)
    var openAt: Instant = openAt
        protected set

    @Column(name = "close_at", nullable = false)
    var closeAt: Instant = closeAt
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: DropStatus = status
        protected set

    private fun transitTo(next: DropStatus) {
        if (!status.canTransitTo(next)) {
            throw InvalidStatusTransitionException("드롭 상태 전이 불가: $status -> $next")
        }
        status = next
    }

    fun open() = transitTo(DropStatus.OPEN)

    fun soldOut() = transitTo(DropStatus.SOLD_OUT)

    fun close() = transitTo(DropStatus.CLOSED)
}
