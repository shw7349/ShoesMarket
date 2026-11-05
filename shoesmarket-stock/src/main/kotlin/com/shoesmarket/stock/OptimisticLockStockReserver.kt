package com.shoesmarket.stock

import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.ReserveResult
import com.shoesmarket.domain.UserId
import com.shoesmarket.infra.persistence.DropStockRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

/**
 * 낙관적 락(@Version + 재시도) 비교 구현 (명세 10.1).
 * 버전 충돌 시 재시도한다. 경합이 심하면 재시도가 늘어 처리량이 떨어지는 특성을 보여준다.
 */
@Component
@ConditionalOnProperty(name = ["shoesmarket.stock.strategy"], havingValue = "OPTIMISTIC")
class OptimisticLockStockReserver(
    private val dropStockRepository: DropStockRepository,
    private val reservationWriter: ReservationWriter,
    transactionManager: PlatformTransactionManager,
) : StockReserver {
    private val transactionTemplate =
        TransactionTemplate(transactionManager).apply {
            propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
        }

    override fun reserve(
        dropId: DropId,
        userId: UserId,
    ): ReserveResult {
        repeat(MAX_RETRY) {
            try {
                return transactionTemplate.execute { attempt(dropId, userId) } ?: ReserveResult.DropNotOpen
            } catch (e: ObjectOptimisticLockingFailureException) {
                // 버전 충돌 — 재시도
            }
        }
        return ReserveResult.SoldOut
    }

    private fun attempt(
        dropId: DropId,
        userId: UserId,
    ): ReserveResult {
        val stock = dropStockRepository.findById(dropId.value).orElse(null) ?: return ReserveResult.DropNotOpen
        if (reservationWriter.alreadyReserved(dropId, userId)) return ReserveResult.AlreadyPurchased
        if (!stock.decrease()) return ReserveResult.SoldOut
        dropStockRepository.saveAndFlush(stock) // 여기서 버전 충돌 시 예외
        return reservationWriter.write(dropId, userId)
    }

    companion object {
        private const val MAX_RETRY = 50
    }
}
