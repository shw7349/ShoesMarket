package com.shoesmarket.api.common

/**
 * 도메인/애플리케이션 예외. ErrorCode를 담아 GlobalExceptionHandler에서 응답으로 변환한다.
 */
class ApiException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.message,
) : RuntimeException(message)
