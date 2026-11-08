package com.shoesmarket.payment

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * 데모용 토스 클라이언트. PG 시크릿 키 없이도 결제가 승인된 것처럼 동작시킨다.
 * shoesmarket.payment.toss.mock=true(기본)일 때 사용되고, 실제 연동 시 false로 바꾸면 RestClient 구현이 쓰인다.
 */
@Component
@ConditionalOnProperty(name = ["shoesmarket.payment.toss.mock"], havingValue = "true", matchIfMissing = true)
class MockTossPaymentClient : TossPaymentClient {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun approve(
        paymentKey: String,
        orderId: String,
        amount: Long,
    ): TossApproval {
        log.info("[MOCK] 결제 승인 orderId={} amount={}", orderId, amount)
        return TossApproval(paymentKey = paymentKey, orderId = orderId, amount = amount, status = "DONE")
    }

    override fun get(paymentKey: String): TossApproval =
        TossApproval(paymentKey = paymentKey, orderId = "mock", amount = 0, status = "DONE")

    override fun getByOrderId(orderId: String): TossApproval =
        TossApproval(paymentKey = "mock", orderId = orderId, amount = 0, status = "DONE")

    override fun cancel(
        paymentKey: String,
        reason: String,
    ) {
        log.info("[MOCK] 결제 취소 paymentKey={} reason={}", paymentKey, reason)
    }
}
