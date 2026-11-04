package com.shoesmarket.stock

import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.ReserveResult
import com.shoesmarket.domain.UserId

/**
 * 재고 선점 전략 인터페이스. 구현체를 설정값(shoesmarket.stock.strategy)으로 교체할 수 있다.
 * Phase 2 기본 구현은 Redis Lua 스크립트([LuaScriptStockReserver]).
 */
interface StockReserver {
    fun reserve(
        dropId: DropId,
        userId: UserId,
    ): ReserveResult
}
