package com.shoesmarket.infra.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version

/**
 * DB 락 비교용 재고 행 (명세 10.1). drop_id를 PK로 공유한다.
 * Lua 전략에서는 쓰지 않고, 비관적/낙관적/Redisson 전략의 재고 차감 대상이다.
 */
@Entity
@Table(name = "drop_stock")
class DropStockEntity(
    dropId: Long,
    remaining: Int,
) {
    @Id
    @Column(name = "drop_id")
    val dropId: Long = dropId

    @Column(name = "remaining", nullable = false)
    var remaining: Int = remaining
        protected set

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0
        protected set

    /** 재고가 있으면 1 차감하고 true, 없으면 false. */
    fun decrease(): Boolean {
        if (remaining <= 0) return false
        remaining -= 1
        return true
    }

    fun increase() {
        remaining += 1
    }
}
