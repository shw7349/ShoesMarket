package com.shoesmarket.payment

import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.util.Base64

/**
 * 토스페이먼츠 RestClient 구현. 시크릿 키는 환경변수로만 주입한다(커밋 금지).
 */
@Component
class TossPaymentRestClient(
    private val properties: TossProperties,
) : TossPaymentClient {
    private val restClient: RestClient by lazy {
        val basic = Base64.getEncoder().encodeToString("${properties.secretKey}:".toByteArray())
        RestClient
            .builder()
            .baseUrl(properties.baseUrl)
            .defaultHeader("Authorization", "Basic $basic")
            .defaultHeader("Content-Type", "application/json")
            .build()
    }

    override fun approve(
        paymentKey: String,
        orderId: String,
        amount: Long,
    ): TossApproval =
        try {
            val response =
                restClient
                    .post()
                    .uri("/v1/payments/confirm")
                    .body(mapOf("paymentKey" to paymentKey, "orderId" to orderId, "amount" to amount))
                    .retrieve()
                    .body(Map::class.java)
            toApproval(response, paymentKey, orderId, amount)
        } catch (e: Exception) {
            throw TossPaymentException("토스 결제 승인 실패: ${e.message}", e)
        }

    override fun get(paymentKey: String): TossApproval =
        try {
            val response =
                restClient
                    .get()
                    .uri("/v1/payments/{paymentKey}", paymentKey)
                    .retrieve()
                    .body(Map::class.java)
            toApproval(
                response,
                paymentKey,
                response?.get("orderId") as? String ?: "",
                (response?.get("totalAmount") as? Number)?.toLong() ?: 0,
            )
        } catch (e: Exception) {
            throw TossPaymentException("토스 결제 조회 실패: ${e.message}", e)
        }

    override fun cancel(
        paymentKey: String,
        reason: String,
    ) {
        try {
            restClient
                .post()
                .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                .body(mapOf("cancelReason" to reason))
                .retrieve()
                .toBodilessEntity()
        } catch (e: Exception) {
            throw TossPaymentException("토스 결제 취소 실패: ${e.message}", e)
        }
    }

    private fun toApproval(
        response: Map<*, *>?,
        paymentKey: String,
        orderId: String,
        amount: Long,
    ): TossApproval =
        TossApproval(
            paymentKey = response?.get("paymentKey") as? String ?: paymentKey,
            orderId = response?.get("orderId") as? String ?: orderId,
            amount = (response?.get("totalAmount") as? Number)?.toLong() ?: amount,
            status = response?.get("status") as? String ?: "DONE",
        )
}
