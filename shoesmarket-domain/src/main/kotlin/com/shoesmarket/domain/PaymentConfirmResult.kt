package com.shoesmarket.domain

/**
 * 결제 승인 결과 (명세 7.2). 컨트롤러에서 when으로 HTTP 응답에 매핑한다.
 */
sealed interface PaymentConfirmResult {
    data class Success(
        val orderId: OrderId,
    ) : PaymentConfirmResult

    /** 멱등 재요청: 이미 승인된 결제의 기존 주문을 그대로 반환. */
    data class AlreadyConfirmed(
        val orderId: OrderId,
    ) : PaymentConfirmResult

    data object ReservationNotFound : PaymentConfirmResult

    data object ReservationExpired : PaymentConfirmResult

    data object AmountMismatch : PaymentConfirmResult

    data object PaymentFailed : PaymentConfirmResult
}
