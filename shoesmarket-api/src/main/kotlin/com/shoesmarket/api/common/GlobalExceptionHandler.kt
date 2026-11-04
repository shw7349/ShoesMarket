package com.shoesmarket.api.common

import com.shoesmarket.domain.InvalidStatusTransitionException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ApiException::class)
    fun handleApiException(e: ApiException): ResponseEntity<ApiResponse<Nothing>> {
        val code = e.errorCode
        return ResponseEntity.status(code.status).body(ApiResponse.fail(ApiError(code.name, e.message)))
    }

    @ExceptionHandler(InvalidStatusTransitionException::class)
    fun handleInvalidTransition(e: InvalidStatusTransitionException): ResponseEntity<ApiResponse<Nothing>> {
        val code = ErrorCode.INVALID_STATUS_TRANSITION
        return ResponseEntity.status(code.status).body(ApiResponse.fail(ApiError(code.name, e.message ?: code.message)))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val code = ErrorCode.INVALID_REQUEST
        val detail = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        val message = detail.ifBlank { code.message }
        return ResponseEntity.status(code.status).body(ApiResponse.fail(ApiError(code.name, message)))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        val code = ErrorCode.INVALID_REQUEST
        return ResponseEntity.status(code.status).body(ApiResponse.fail(ApiError(code.name, e.message ?: code.message)))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.error("예상치 못한 오류", e)
        val code = ErrorCode.INTERNAL_ERROR
        return ResponseEntity.status(code.status).body(ApiResponse.fail(ApiError(code.name, code.message)))
    }
}
