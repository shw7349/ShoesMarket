package com.shoesmarket.infra.persistence

import com.shoesmarket.domain.DropStatus
import com.shoesmarket.domain.PaymentStatus
import com.shoesmarket.domain.ReservationStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface ProductRepository : JpaRepository<ProductEntity, Long>

interface DropEventRepository : JpaRepository<DropEventEntity, Long> {
    fun findByStatus(status: DropStatus): List<DropEventEntity>

    fun findByProductId(productId: Long): List<DropEventEntity>

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

    fun findByUserIdOrderByIdDesc(userId: Long): List<ReservationEntity>

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

    fun findByUserIdOrderByIdDesc(userId: Long): List<OrderEntity>

    /** 해당 회원이 그 상품의 드롭에서 결제 완료한 주문이 있는지(후기 작성 자격). */
    @Query(
        "select count(o) > 0 from OrderEntity o, DropEventEntity d " +
            "where o.dropId = d.id and o.userId = :userId and d.productId = :productId " +
            "and o.status = com.shoesmarket.domain.OrderStatus.PAID",
    )
    fun hasPurchasedProduct(
        @Param("userId") userId: Long,
        @Param("productId") productId: Long,
    ): Boolean
}

interface ReviewRepository : JpaRepository<ReviewEntity, Long> {
    fun findByProductIdAndHiddenFalse(
        productId: Long,
        pageable: org.springframework.data.domain.Pageable,
    ): org.springframework.data.domain.Page<ReviewEntity>

    fun countByProductIdAndHiddenFalse(productId: Long): Long

    @Query("select coalesce(avg(r.rating), 0) from ReviewEntity r where r.productId = :productId and r.hidden = false")
    fun averageRating(
        @Param("productId") productId: Long,
    ): Double
}

interface MemberRepository : JpaRepository<MemberEntity, Long> {
    fun findByEmail(email: String): MemberEntity?

    fun existsByEmail(email: String): Boolean
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

interface DropStockRepository : JpaRepository<DropStockEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from DropStockEntity s where s.dropId = :dropId")
    fun findByDropIdForUpdate(
        @Param("dropId") dropId: Long,
    ): DropStockEntity?
}
