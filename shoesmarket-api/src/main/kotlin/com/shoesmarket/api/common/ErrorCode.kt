package com.shoesmarket.api.common

import org.springframework.http.HttpStatus

/**
 * 도메인 에러 코드 (SPEC 6.3). @RestControllerAdvice에서 일괄 매핑한다.
 */
enum class ErrorCode(
    val status: HttpStatus,
    val message: String,
) {
    DROP_NOT_FOUND(HttpStatus.NOT_FOUND, "드롭을 찾을 수 없습니다."),
    DROP_NOT_OPEN(HttpStatus.BAD_REQUEST, "드롭이 오픈 상태가 아닙니다."),
    ALREADY_IN_QUEUE(HttpStatus.CONFLICT, "이미 대기열에 있습니다."),
    INVALID_TOKEN(HttpStatus.FORBIDDEN, "유효하지 않은 입장 토큰입니다."),
    SOLD_OUT(HttpStatus.CONFLICT, "품절되었습니다."),
    ALREADY_PURCHASED(HttpStatus.CONFLICT, "이미 구매한 사용자입니다."),
    RESERVATION_EXPIRED(HttpStatus.CONFLICT, "선점이 만료되었습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "선점 내역을 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다."),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "결제에 실패했습니다."),
    DUPLICATE_REQUEST(HttpStatus.CONFLICT, "중복된 요청입니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, "허용되지 않은 상태 전이입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
}
