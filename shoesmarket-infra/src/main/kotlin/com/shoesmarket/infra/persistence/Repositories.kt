package com.shoesmarket.infra.persistence

import com.shoesmarket.domain.DropStatus
import com.shoesmarket.domain.ReservationStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface ProductRepository : JpaRepository<ProductEntity, Long>

interface DropEventRepository : JpaRepository<DropEventEntity, Long> {
    fun findByStatus(status: DropStatus): List<DropEventEntity>

    fun findByStatusAndOpenAtLessThanEqual(
        status: DropStatus,
        openAt: Instant,
    ): List<DropEventEntity>

    fun findByStatusInAndCloseAtLessThanEqual(
        statuses: Collection<DropStatus>,
        closeAt: Instant,
    ): List<DropEventEntity>
}

interface ReservationRepository : JpaRepository<ReservationEntity, Long> {
    fun findByDropIdAndUserId(
        dropId: Long,
        userId: Long,
    ): ReservationEntity?

    fun findTop100ByStatusAndExpiresAtBefore(
        status: ReservationStatus,
        expiresAt: Instant,
    ): List<ReservationEntity>
}

interface OrderRepository : JpaRepository<OrderEntity, Long> {
    fun findByReservationId(reservationId: Long): OrderEntity?
}

interface PaymentRepository : JpaRepository<PaymentEntity, Long> {
    fun findByIdempotencyKey(idempotencyKey: String): PaymentEntity?
}

interface IncidentRepository : JpaRepository<IncidentEntity, Long>
