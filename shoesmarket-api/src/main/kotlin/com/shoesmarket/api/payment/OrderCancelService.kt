package com.shoesmarket.api.payment

import com.shoesmarket.api.common.ApiException
import com.shoesmarket.api.common.ErrorCode
import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.DropStatus
import com.shoesmarket.domain.UserId
import com.shoesmarket.infra.persistence.DropEventRepository
import com.shoesmarket.infra.persistence.OrderRepository
import com.shoesmarket.infra.persistence.PaymentRepository
import com.shoesmarket.infra.persistence.ReservationRepository
import com.shoesmarket.payment.TossPaymentClient
import com.shoesmarket.stock.StockReleaser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 주문 취소(환불). 재고 반환 정책: 드롭 진행 중이면 반환, 종료(CLOSED) 후면 반환하지 않는다.
 */
@Service
class OrderCancelService(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val reservationRepository: ReservationRepository,
    private val dropEventRepository: DropEventRepository,
    private val stockReleaser: StockReleaser,
    private val tossClient: TossPaymentClient,
) {
    @Transactional
    fun cancel(orderId: Long) {
        val order =
            orderRepository.findById(orderId).orElseThrow { ApiException(ErrorCode.ORDER_NOT_FOUND) }

        paymentRepository.findByIdempotencyKey("reservation:${order.reservationId}")?.pgPaymentKey?.let { key ->
            runCatching { tossClient.cancel(key, "주문 취소") }
        }

        order.cancel()
        reservationRepository.findById(order.reservationId).ifPresent { it.cancel() }

        val drop = dropEventRepository.findById(order.dropId).orElse(null)
        if (drop != null && (drop.status == DropStatus.OPEN || drop.status == DropStatus.SOLD_OUT)) {
            stockReleaser.release(DropId(order.dropId), UserId(order.userId))
        }
    }
}
