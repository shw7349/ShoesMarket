package com.shoesmarket.api.review

import com.shoesmarket.api.common.ApiException
import com.shoesmarket.api.common.ApiResponse
import com.shoesmarket.api.common.ErrorCode
import com.shoesmarket.commerce.ReviewService
import com.shoesmarket.commerce.ReviewSort
import com.shoesmarket.infra.persistence.MemberRepository
import com.shoesmarket.infra.persistence.ReviewEntity
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@Tag(name = "Review", description = "후기 게시판 API")
@RestController
class ReviewController(
    private val reviewService: ReviewService,
    private val memberRepository: MemberRepository,
) {
    @Operation(summary = "후기 작성 (구매자만)")
    @PostMapping("/api/v1/products/{productId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    fun write(
        @PathVariable productId: Long,
        @RequestHeader(value = "X-User-Id", required = false) userIdHeader: Long?,
        @Valid @RequestBody request: ReviewRequest,
    ): ApiResponse<ReviewResponse> {
        val memberId = userIdHeader ?: throw ApiException(ErrorCode.UNAUTHORIZED)
        val authorName = memberRepository.findById(memberId).map { it.displayName }.orElse("회원")
        val saved = reviewService.write(productId, memberId, authorName, request.rating, request.content, request.imageUrl)
        return ApiResponse.success(ReviewResponse.from(saved))
    }

    @Operation(summary = "후기 목록 + 평점 요약")
    @GetMapping("/api/v1/products/{productId}/reviews")
    fun list(
        @PathVariable productId: Long,
        @RequestParam(defaultValue = "LATEST") sort: ReviewSort,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ApiResponse<ReviewListResponse> {
        val summary = reviewService.summary(productId)
        val reviews = reviewService.list(productId, sort, page, size)
        return ApiResponse.success(
            ReviewListResponse(
                average = summary.average,
                count = summary.count,
                page = reviews.number,
                totalPages = reviews.totalPages,
                reviews = reviews.content.map { ReviewResponse.from(it) },
            ),
        )
    }

    @Operation(summary = "후기 수정 (작성자만)")
    @PutMapping("/api/v1/reviews/{reviewId}")
    fun update(
        @PathVariable reviewId: Long,
        @RequestHeader(value = "X-User-Id", required = false) userIdHeader: Long?,
        @Valid @RequestBody request: ReviewRequest,
    ): ApiResponse<ReviewResponse> {
        val memberId = userIdHeader ?: throw ApiException(ErrorCode.UNAUTHORIZED)
        val updated = reviewService.update(reviewId, memberId, request.rating, request.content, request.imageUrl)
        return ApiResponse.success(ReviewResponse.from(updated))
    }

    @Operation(summary = "후기 삭제 (작성자만)")
    @DeleteMapping("/api/v1/reviews/{reviewId}")
    fun delete(
        @PathVariable reviewId: Long,
        @RequestHeader(value = "X-User-Id", required = false) userIdHeader: Long?,
    ): ApiResponse<Map<String, Long>> {
        val memberId = userIdHeader ?: throw ApiException(ErrorCode.UNAUTHORIZED)
        reviewService.delete(reviewId, memberId)
        return ApiResponse.success(mapOf("reviewId" to reviewId))
    }

    @Operation(summary = "후기 숨김 (관리자)")
    @PostMapping("/api/v1/admin/reviews/{reviewId}/hide")
    fun hide(
        @PathVariable reviewId: Long,
    ): ApiResponse<Map<String, Long>> {
        reviewService.hide(reviewId)
        return ApiResponse.success(mapOf("reviewId" to reviewId))
    }
}

data class ReviewRequest(
    @field:Min(1) @field:Max(5)
    val rating: Int,
    @field:NotBlank(message = "내용은 필수입니다.")
    val content: String,
    val imageUrl: String? = null,
)

data class ReviewResponse(
    val id: Long,
    val authorName: String,
    val rating: Int,
    val content: String,
    val imageUrl: String?,
    val createdAt: Instant,
) {
    companion object {
        fun from(e: ReviewEntity): ReviewResponse = ReviewResponse(e.id, e.authorName, e.rating, e.content, e.imageUrl, e.createdAt)
    }
}

data class ReviewListResponse(
    val average: Double,
    val count: Long,
    val page: Int,
    val totalPages: Int,
    val reviews: List<ReviewResponse>,
)
