package com.shoesmarket.stock

import com.shoesmarket.domain.DropId
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

/**
 * 드롭 오픈 시 Redis 재고를 초기화한다. 이미 있으면 덮어쓰지 않는다(SET NX, 명세 5.5).
 */
@Component
class StockRedisInitializer(
    private val redisTemplate: StringRedisTemplate,
) {
    fun initialize(
        dropId: DropId,
        totalStock: Int,
    ): Boolean = redisTemplate.opsForValue().setIfAbsent(StockKeys.stock(dropId), totalStock.toString()) ?: false
}
