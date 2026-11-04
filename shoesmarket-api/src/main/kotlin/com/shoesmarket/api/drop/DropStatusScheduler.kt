package com.shoesmarket.api.drop

import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.DropStatus
import com.shoesmarket.infra.persistence.DropEventRepository
import com.shoesmarket.stock.StockRedisInitializer
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

/**
 * 드롭 상태 전이 스케줄러 (명세 8장 dropStatusJob).
 * open_at 도달 → OPEN + Redis 재고 초기화, close_at 도달 → CLOSED.
 */
@Component
class DropStatusScheduler(
    private val dropEventRepository: DropEventRepository,
    private val stockRedisInitializer: StockRedisInitializer,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${shoesmarket.drop.status-interval-ms:60000}")
    @Transactional
    fun run() {
        val now = clock.instant()

        dropEventRepository.findByStatusAndOpenAtLessThanEqual(DropStatus.SCHEDULED, now).forEach { drop ->
            drop.open()
            val initialized = stockRedisInitializer.initialize(DropId(drop.id), drop.totalStock)
            log.info("드롭 오픈 dropId={} 재고초기화={}", drop.id, initialized)
        }

        dropEventRepository
            .findByStatusInAndCloseAtLessThanEqual(listOf(DropStatus.OPEN, DropStatus.SOLD_OUT), now)
            .forEach { drop ->
                drop.close()
                log.info("드롭 종료 dropId={}", drop.id)
            }
    }
}
