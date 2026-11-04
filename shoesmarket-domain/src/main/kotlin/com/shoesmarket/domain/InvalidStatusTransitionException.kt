package com.shoesmarket.domain

/**
 * 허용되지 않은 상태 전이를 시도할 때 던진다.
 */
class InvalidStatusTransitionException(
    message: String,
) : RuntimeException(message)
