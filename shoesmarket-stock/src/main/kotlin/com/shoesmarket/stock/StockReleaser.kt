package com.shoesmarket.stock

import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.UserId
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component

/**
 * 재고 반환(명세 5.4). SREM 결과로 중복 반환을 막는다 — 두 번 호출해도 재고는 한 번만 증가한다.
 */
@Component
class StockReleaser(
    private val redisTemplate: StringRedisTemplate,
    @Qualifier("releaseStockScript") private val releaseScript: RedisScript<Long>,
) {
    /** 반환 성공 시 증가된 재고, 이미 반환됐으면 -1. */
    fun release(
        dropId: DropId,
        userId: UserId,
    ): Long =
        redisTemplate.execute(
            releaseScript,
            listOf(StockKeys.stock(dropId), StockKeys.buyers(dropId)),
            userId.value.toString(),
        ) ?: -1L
}
