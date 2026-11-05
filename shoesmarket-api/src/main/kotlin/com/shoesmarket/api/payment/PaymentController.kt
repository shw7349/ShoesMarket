package com.shoesmarket.api.payment

import com.shoesmarket.api.common.ApiException
import com.shoesmarket.api.common.ApiResponse
import com.shoesmarket.api.common.ErrorCode
import com.shoesmarket.domain.PaymentConfirmResult
import com.shoesmarket.payment.PaymentConfirmCommand
import com.shoesmarket.payment.PaymentConfirmService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Payment", description = "결제 API")
@RestController
class PaymentController(
    private val paymentConfirmService: PaymentConfirmService,
) {
    @Operation(summary = "결제 승인", description = "토스 successUrl 이후 호출. 멱등성 키로 중복 승인을 막는다.")
    @PostMapping("/api/v1/payments/confirm")
    fun confirm(
        @jakarta.validation.Valid @RequestBody request: PaymentConfirmRequest,
    ): ApiResponse<PaymentConfirmResponse> {
        val command =
            PaymentConfirmCommand(
                paymentKey = request.paymentKey,
                pgOrderId = request.orderId,
                amount = request.amount,
                reservationId = request.reservationId,
            )
        return when (val result = paymentConfirmService.confirm(command)) {
            is PaymentConfirmResult.Success -> ApiResponse.success(PaymentConfirmResponse(result.orderId.value))
            is PaymentConfirmResult.AlreadyConfirmed -> ApiResponse.success(PaymentConfirmResponse(result.orderId.value))
            PaymentConfirmResult.ReservationNotFound -> throw ApiException(ErrorCode.RESERVATION_NOT_FOUND)
            PaymentConfirmResult.ReservationExpired -> throw ApiException(ErrorCode.RESERVATION_EXPIRED)
            PaymentConfirmResult.AmountMismatch -> throw ApiException(ErrorCode.PAYMENT_AMOUNT_MISMATCH)
            PaymentConfirmResult.PaymentFailed -> throw ApiException(ErrorCode.PAYMENT_FAILED)
        }
    }
}

data class PaymentConfirmRequest(
    @field:NotBlank(message = "paymentKey는 필수입니다.")
    val paymentKey: String,
    @field:NotBlank(message = "orderId(PG)는 필수입니다.")
    val orderId: String,
    @field:Min(value = 0, message = "금액은 0 이상이어야 합니다.")
    val amount: Long,
    @field:NotNull(message = "reservationId는 필수입니다.")
    val reservationId: Long,
)

data class PaymentConfirmResponse(
    val orderId: Long,
)
