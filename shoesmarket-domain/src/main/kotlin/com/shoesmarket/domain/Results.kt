package com.shoesmarket.domain

import java.time.Instant

/**
 * 재고 선점 결과. 컨트롤러에서 when으로 HTTP 응답에 매핑한다.
 */
sealed interface ReserveResult {
    data class Success(val reservationId: ReservationId, val expiresAt: Instant) : ReserveResult

    data object SoldOut : ReserveResult

    data object AlreadyPurchased : ReserveResult

    data object InvalidToken : ReserveResult

    data object DropNotOpen : ReserveResult
}

/**
 * 대기열 진입 결과.
 */
sealed interface QueueEnterResult {
    data class Waiting(val rank: Long) : QueueEnterResult

    data object AlreadyInQueue : QueueEnterResult

    data object DropNotOpen : QueueEnterResult
}
