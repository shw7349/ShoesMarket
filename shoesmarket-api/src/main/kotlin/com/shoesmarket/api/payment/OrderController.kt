package com.shoesmarket.api.payment

import com.shoesmarket.api.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Order", description = "주문 API")
@RestController
class OrderController(
    private val orderCancelService: OrderCancelService,
) {
    @Operation(summary = "주문 취소(환불)")
    @PostMapping("/api/v1/orders/{orderId}/cancel")
    fun cancel(
        @PathVariable orderId: Long,
    ): ApiResponse<Map<String, Long>> {
        orderCancelService.cancel(orderId)
        return ApiResponse.success(mapOf("orderId" to orderId))
    }
}
