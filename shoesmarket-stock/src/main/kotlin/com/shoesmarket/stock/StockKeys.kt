package com.shoesmarket.stock

import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.UserId

/**
 * Redis 키 설계 (명세 5.1).
 */
object StockKeys {
    fun stock(dropId: DropId): String = "drop:${dropId.value}:stock"

    fun buyers(dropId: DropId): String = "drop:${dropId.value}:buyers"

    fun token(
        dropId: DropId,
        userId: UserId,
    ): String = "drop:${dropId.value}:token:${userId.value}"
}
