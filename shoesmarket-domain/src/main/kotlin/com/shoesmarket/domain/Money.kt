package com.shoesmarket.domain

/**
 * 금액. 원 단위 [Long]으로 저장한다. BigDecimal/Double 사용 금지.
 */
@JvmInline
value class Money(val amount: Long) {
    init {
        require(amount >= 0) { "금액은 0 이상이어야 합니다." }
    }

    operator fun plus(other: Money): Money = Money(amount + other.amount)

    operator fun times(quantity: Int): Money = Money(amount * quantity)

    companion object {
        val ZERO = Money(0)
    }
}
