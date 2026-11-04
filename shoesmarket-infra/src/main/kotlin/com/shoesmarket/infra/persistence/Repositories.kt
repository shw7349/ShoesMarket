package com.shoesmarket.infra.persistence

import com.shoesmarket.domain.ReservationStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface ProductRepository : JpaRepository<ProductEntity, Long>

interface DropEventRepository : JpaRepository<DropEventEntity, Long>

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
