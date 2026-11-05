package com.shoesmarket.stock

import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.ReserveResult
import com.shoesmarket.domain.UserId
import com.shoesmarket.infra.persistence.DropStockRepository
import org.redisson.api.RedissonClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.TimeUnit

/**
 * Redisson 분산 락 비교 구현 (명세 10.1).
 * drop별 분산 락을 잡고 DB 재고를 차감한다. 애플리케이션이 여러 대일 때도 상호 배제를 보장하지만
 * 락 획득/해제 왕복 비용이 든다.
 */
@Component
@ConditionalOnProperty(name = ["shoesmarket.stock.strategy"], havingValue = "REDISSON")
class RedissonLockStockReserver(
    private val redissonClient: RedissonClient,
    private val dropStockRepository: DropStockRepository,
    private val reservationWriter: ReservationWriter,
    transactionManager: PlatformTransactionManager,
) : StockReserver {
    private val transactionTemplate = TransactionTemplate(transactionManager)

    override fun reserve(
        dropId: DropId,
        userId: UserId,
    ): ReserveResult {
        val lock = redissonClient.getLock("drop:${dropId.value}:lock")
        if (!lock.tryLock(3, 3, TimeUnit.SECONDS)) {
            return ReserveResult.SoldOut
        }
        return try {
            transactionTemplate.execute { attempt(dropId, userId) } ?: ReserveResult.DropNotOpen
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }

    private fun attempt(
        dropId: DropId,
        userId: UserId,
    ): ReserveResult {
        val stock = dropStockRepository.findById(dropId.value).orElse(null) ?: return ReserveResult.DropNotOpen
        if (reservationWriter.alreadyReserved(dropId, userId)) return ReserveResult.AlreadyPurchased
        if (!stock.decrease()) return ReserveResult.SoldOut
        dropStockRepository.save(stock)
        return reservationWriter.write(dropId, userId)
    }
}
