package com.shoesmarket.domain

/**
 * 장애 분류. 원인 분석과 Notion 기록 시 카테고리로 쓴다.
 */
enum class IncidentCategory {
    DATA_INTEGRITY, // DB 제약 위반(중복/유니크 등)
    REDIS_FAILURE, // Redis 연결/명령 실패
    STATUS_TRANSITION, // 허용되지 않은 상태 전이
    PAYMENT, // 결제/PG 연동 실패
    STOCK_CONSISTENCY, // 재고 정합성 불일치
    UNKNOWN, // 미분류
}
