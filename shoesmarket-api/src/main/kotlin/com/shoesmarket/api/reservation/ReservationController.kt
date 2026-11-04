package com.shoesmarket.api.reservation

import com.shoesmarket.api.common.ApiException
import com.shoesmarket.api.common.ApiResponse
import com.shoesmarket.api.common.ErrorCode
import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.ReserveResult
import com.shoesmarket.domain.UserId
import com.shoesmarket.stock.StockReserver
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@Tag(name = "Reservation", description = "재고 선점 API")
@RestController
class ReservationController(
    private val stockReserver: StockReserver,
) {
    @Operation(summary = "재고 선점", description = "입장 토큰으로 재고를 선점한다. X-User-Id 헤더 필요.")
    @PostMapping("/api/v1/drops/{dropId}/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    fun reserve(
        @PathVariable dropId: Long,
        @RequestHeader(value = "X-User-Id", required = false) userIdHeader: Long?,
    ): ApiResponse<ReservationResponse> {
        val userId = userIdHeader?.let { UserId(it) } ?: throw ApiException(ErrorCode.UNAUTHORIZED)
        return when (val result = stockReserver.reserve(DropId(dropId), userId)) {
            is ReserveResult.Success ->
                ApiResponse.success(ReservationResponse(result.reservationId.value, result.expiresAt))
            ReserveResult.SoldOut -> throw ApiException(ErrorCode.SOLD_OUT)
            ReserveResult.AlreadyPurchased -> throw ApiException(ErrorCode.ALREADY_PURCHASED)
            ReserveResult.InvalidToken -> throw ApiException(ErrorCode.INVALID_TOKEN)
            ReserveResult.DropNotOpen -> throw ApiException(ErrorCode.DROP_NOT_OPEN)
        }
    }
}

data class ReservationResponse(
    val reservationId: Long,
    val expiresAt: Instant,
)
