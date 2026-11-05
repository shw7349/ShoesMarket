package com.shoesmarket.stock

import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.ReserveResult
import com.shoesmarket.domain.UserId
import com.shoesmarket.infra.persistence.DropStockRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 비관적 락(SELECT ... FOR UPDATE) 비교 구현 (명세 10.1).
 * 재고 행을 잠근 채로 1인 제한 확인과 차감을 수행하므로 경합을 직렬화한다.
 */
@Component
@ConditionalOnProperty(name = ["shoesmarket.stock.strategy"], havingValue = "PESSIMISTIC")
class PessimisticLockStockReserver(
    private val dropStockRepository: DropStockRepository,
    private val reservationWriter: ReservationWriter,
) : StockReserver {
    @Transactional
    override fun reserve(
        dropId: DropId,
        userId: UserId,
    ): ReserveResult {
        val stock = dropStockRepository.findByDropIdForUpdate(dropId.value) ?: return ReserveResult.DropNotOpen
        if (reservationWriter.alreadyReserved(dropId, userId)) return ReserveResult.AlreadyPurchased
        if (!stock.decrease()) return ReserveResult.SoldOut
        return reservationWriter.write(dropId, userId)
    }
}
