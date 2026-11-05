package com.shoesmarket.payment

/**
 * 토스페이먼츠 연동 (명세 4단계: 승인/조회/취소). 테스트에서는 이 인터페이스를 Mock으로 대체한다.
 */
interface TossPaymentClient {
    /** 결제 승인. 실패 시 [TossPaymentException]을 던진다. */
    fun approve(
        paymentKey: String,
        orderId: String,
        amount: Long,
    ): TossApproval

    /** 결제 조회(대사 배치에서 사용). */
    fun get(paymentKey: String): TossApproval

    /** 결제 취소(승인 후 경합/환불). */
    fun cancel(
        paymentKey: String,
        reason: String,
    )
}

data class TossApproval(
    val paymentKey: String,
    val orderId: String,
    val amount: Long,
    val status: String,
)

class TossPaymentException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
