package com.shoesmarket.infra.persistence

import com.shoesmarket.domain.DropStatus
import com.shoesmarket.domain.PaymentStatus
import com.shoesmarket.domain.ReservationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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

    fun countByDropIdAndStatus(
        dropId: Long,
        status: ReservationStatus,
    ): Long

    /** RESERVED일 때만 CONFIRMED로 전이. 영향 행 수를 반환(0이면 만료 등과 경합). */
    @Modifying(clearAutomatically = true)
    @Query(
        "update ReservationEntity r set r.status = com.shoesmarket.domain.ReservationStatus.CONFIRMED " +
            "where r.id = :id and r.status = com.shoesmarket.domain.ReservationStatus.RESERVED",
    )
    fun confirmIfReserved(
        @Param("id") id: Long,
    ): Int

    /** RESERVED일 때만 EXPIRED로 전이. 영향 행 1일 때만 재고를 반환해야 한다. */
    @Modifying(clearAutomatically = true)
    @Query(
        "update ReservationEntity r set r.status = com.shoesmarket.domain.ReservationStatus.EXPIRED " +
            "where r.id = :id and r.status = com.shoesmarket.domain.ReservationStatus.RESERVED",
    )
    fun expireIfReserved(
        @Param("id") id: Long,
    ): Int
}

interface OrderRepository : JpaRepository<OrderEntity, Long> {
    fun findByReservationId(reservationId: Long): OrderEntity?
}

interface PaymentRepository : JpaRepository<PaymentEntity, Long> {
    fun findByIdempotencyKey(idempotencyKey: String): PaymentEntity?

    fun findByStatusAndCreatedAtBefore(
        status: PaymentStatus,
        createdAt: Instant,
    ): List<PaymentEntity>
}

interface IncidentRepository : JpaRepository<IncidentEntity, Long>

interface ConsistencyReportRepository : JpaRepository<ConsistencyReportEntity, Long>
