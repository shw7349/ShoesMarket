package com.shoesmarket.queue

import com.shoesmarket.domain.DropId

/**
 * 대기열 Redis 키 (명세 5.1). 토큰 키는 재고 선점(StockKeys.token)과 동일한 형식을 써서
 * 입장 스케줄러가 발급한 토큰을 선점 Lua가 그대로 인식한다.
 */
object QueueKeys {
    fun queue(dropId: DropId): String = "drop:${dropId.value}:queue"

    fun token(
        dropId: DropId,
        userId: String,
    ): String = "drop:${dropId.value}:token:$userId"
}
