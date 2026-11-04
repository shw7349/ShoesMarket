package com.shoesmarket.api.common

/**
 * 공통 응답 포맷 (SPEC 6.1).
 * 성공: { success: true, data: ..., error: null }
 * 실패: { success: false, data: null, error: { code, message } }
 */
data class ApiResponse<out T>(
    val success: Boolean,
    val data: T?,
    val error: ApiError?,
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(success = true, data = data, error = null)

        fun fail(error: ApiError): ApiResponse<Nothing> = ApiResponse(success = false, data = null, error = error)
    }
}

data class ApiError(
    val code: String,
    val message: String,
)
