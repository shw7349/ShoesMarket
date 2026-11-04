package com.shoesmarket.domain

/**
 * 드롭(판매 이벤트) 상태.
 * SCHEDULED → OPEN → CLOSED, OPEN → SOLD_OUT → CLOSED
 */
enum class DropStatus {
    SCHEDULED,
    OPEN,
    SOLD_OUT,
    CLOSED,
    ;

    fun canTransitTo(next: DropStatus): Boolean =
        when (this) {
            SCHEDULED -> next == OPEN
            OPEN -> next == SOLD_OUT || next == CLOSED
            SOLD_OUT -> next == CLOSED
            CLOSED -> false
        }
}

/**
 * 재고 선점 상태.
 */
enum class ReservationStatus {
    RESERVED,
    CONFIRMED,
    EXPIRED,
    CANCELLED,
    ;

    fun canTransitTo(next: ReservationStatus): Boolean =
        when (this) {
            RESERVED -> next in setOf(CONFIRMED, EXPIRED, CANCELLED)
            CONFIRMED -> next == CANCELLED
            EXPIRED, CANCELLED -> false
        }
}

/**
 * 주문 상태. PAID → CANCELLED (환불)
 */
enum class OrderStatus {
    PAID,
    CANCELLED,
    ;

    fun canTransitTo(next: OrderStatus): Boolean =
        when (this) {
            PAID -> next == CANCELLED
            CANCELLED -> false
        }
}

/**
 * 결제 상태. READY → APPROVED → CANCELLED, READY → FAILED
 */
enum class PaymentStatus {
    READY,
    APPROVED,
    FAILED,
    CANCELLED,
    ;

    fun canTransitTo(next: PaymentStatus): Boolean =
        when (this) {
            READY -> next == APPROVED || next == FAILED
            APPROVED -> next == CANCELLED
            FAILED, CANCELLED -> false
        }
}
