package com.shoesmarket.infra.persistence

import com.shoesmarket.domain.InvalidStatusTransitionException
import com.shoesmarket.domain.ReservationStatus
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
@Table(name = "reservation")
class ReservationEntity(
    dropId: Long,
    userId: Long,
    expiresAt: Instant,
    status: ReservationStatus = ReservationStatus.RESERVED,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(name = "drop_id", nullable = false)
    var dropId: Long = dropId
        protected set

    @Column(name = "user_id", nullable = false)
    var userId: Long = userId
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: ReservationStatus = status
        protected set

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant = expiresAt
        protected set

    private fun transitTo(next: ReservationStatus) {
        if (!status.canTransitTo(next)) {
            throw InvalidStatusTransitionException("선점 상태 전이 불가: $status -> $next")
        }
        status = next
    }

    fun confirm() = transitTo(ReservationStatus.CONFIRMED)

    fun expire() = transitTo(ReservationStatus.EXPIRED)

    fun cancel() = transitTo(ReservationStatus.CANCELLED)
}
