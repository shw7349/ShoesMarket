package com.shoesmarket.infra.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * 재고 정합성 검증 결과 (명세 8장 stockConsistencyJob). 드롭 종료 후 Redis-DB 재고를 대조해 기록한다.
 */
@Entity
@Table(name = "consistency_report")
class ConsistencyReportEntity(
    dropId: Long,
    totalStock: Int,
    confirmedCount: Long,
    reservedCount: Long,
    redisRemaining: Int,
    consistent: Boolean,
    detail: String,
    checkedAt: Instant,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(name = "drop_id", nullable = false)
    var dropId: Long = dropId
        protected set

    @Column(name = "total_stock", nullable = false)
    var totalStock: Int = totalStock
        protected set

    @Column(name = "confirmed_count", nullable = false)
    var confirmedCount: Long = confirmedCount
        protected set

    @Column(name = "reserved_count", nullable = false)
    var reservedCount: Long = reservedCount
        protected set

    @Column(name = "redis_remaining", nullable = false)
    var redisRemaining: Int = redisRemaining
        protected set

    @Column(name = "consistent", nullable = false)
    var consistent: Boolean = consistent
        protected set

    @Column(name = "detail", nullable = false, length = 1000)
    var detail: String = detail
        protected set

    @Column(name = "checked_at", nullable = false)
    var checkedAt: Instant = checkedAt
        protected set
}
