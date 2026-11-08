package com.shoesmarket.api.common

import com.shoesmarket.api.incident.IncidentReporter
import com.shoesmarket.commerce.NotPurchasedException
import com.shoesmarket.commerce.ReviewForbiddenException
import com.shoesmarket.commerce.ReviewNotFoundException
import com.shoesmarket.domain.InvalidStatusTransitionException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler(
    private val incidentReporter: IncidentReporter,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ApiException::class)
    fun handleApiException(e: ApiException): ResponseEntity<ApiResponse<Nothing>> {
        val code = e.errorCode
        return ResponseEntity.status(code.status).body(ApiResponse.fail(ApiError(code.name, e.message)))
    }

    @ExceptionHandler(InvalidStatusTransitionException::class)
    fun handleInvalidTransition(
        e: InvalidStatusTransitionException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Nothing>> {
        val code = ErrorCode.INVALID_STATUS_TRANSITION
        reportIncident(e, request)
        return ResponseEntity.status(code.status).body(ApiResponse.fail(ApiError(code.name, e.message ?: code.message)))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val code = ErrorCode.INVALID_REQUEST
        val detail = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        val message = detail.ifBlank { code.message }
        return ResponseEntity.status(code.status).body(ApiResponse.fail(ApiError(code.name, message)))
    }

    @ExceptionHandler(NotPurchasedException::class)
    fun handleNotPurchased(e: NotPurchasedException): ResponseEntity<ApiResponse<Nothing>> =
        errorResponse(ErrorCode.REVIEW_NOT_PURCHASED, e.message)

    @ExceptionHandler(ReviewForbiddenException::class)
    fun handleReviewForbidden(e: ReviewForbiddenException): ResponseEntity<ApiResponse<Nothing>> =
        errorResponse(ErrorCode.REVIEW_FORBIDDEN, e.message)

    @ExceptionHandler(ReviewNotFoundException::class)
    fun handleReviewNotFound(e: ReviewNotFoundException): ResponseEntity<ApiResponse<Nothing>> =
        errorResponse(ErrorCode.REVIEW_NOT_FOUND, e.message)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        val code = ErrorCode.INVALID_REQUEST
        return ResponseEntity.status(code.status).body(ApiResponse.fail(ApiError(code.name, e.message ?: code.message)))
    }

    // checked / unchecked / 네트워크 I/O 오류를 모두 포괄하도록 Throwable까지 받는다.
    @ExceptionHandler(Throwable::class)
    fun handleUnexpected(
        e: Throwable,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Nothing>> {
        log.error("예상치 못한 오류", e)
        reportIncident(e, request)
        val code = ErrorCode.INTERNAL_ERROR
        return ResponseEntity.status(code.status).body(ApiResponse.fail(ApiError(code.name, code.message)))
    }

    private fun errorResponse(
        code: ErrorCode,
        message: String?,
    ): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(code.status).body(ApiResponse.fail(ApiError(code.name, message ?: code.message)))

    /** 장애를 기록한다. 기록 자체가 실패해도 원래 응답에는 영향을 주지 않는다. */
    private fun reportIncident(
        e: Throwable,
        request: HttpServletRequest,
    ) {
        runCatching {
            incidentReporter.report(
                source = "${request.method} ${request.requestURI}",
                throwable = e,
            )
        }.onFailure { log.warn("장애 기록 실패(무시): {}", it.message) }
    }
}
