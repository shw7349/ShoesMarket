package com.shoesmarket.batch

import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.ReservationStatus
import com.shoesmarket.infra.persistence.ConsistencyReportEntity
import com.shoesmarket.infra.persistence.ConsistencyReportRepository
import com.shoesmarket.infra.persistence.DropEventRepository
import com.shoesmarket.infra.persistence.ReservationRepository
import com.shoesmarket.stock.StockKeys
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

/**
 * 재고 정합성 검증 (명세 8장). total_stock == CONFIRMED + RESERVED + Redis 남은 재고 인지 대조해 기록한다.
 */
@Service
class StockConsistencyService(
    private val reservationRepository: ReservationRepository,
    private val dropEventRepository: DropEventRepository,
    private val consistencyReportRepository: ConsistencyReportRepository,
    private val redisTemplate: StringRedisTemplate,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun check(dropId: Long): ConsistencyReportEntity {
        val drop =
            dropEventRepository.findById(dropId).orElseThrow { IllegalArgumentException("드롭을 찾을 수 없습니다: $dropId") }
        val confirmed = reservationRepository.countByDropIdAndStatus(dropId, ReservationStatus.CONFIRMED)
        val reserved = reservationRepository.countByDropIdAndStatus(dropId, ReservationStatus.RESERVED)
        val redisRemaining =
            redisTemplate.opsForValue().get(StockKeys.stock(DropId(dropId)))?.toIntOrNull() ?: 0

        val sum = confirmed + reserved + redisRemaining
        val consistent = sum == drop.totalStock.toLong()
        val detail =
            "total=${drop.totalStock}, confirmed=$confirmed, reserved=$reserved, redis=$redisRemaining, sum=$sum"
        if (!consistent) {
            log.warn("재고 정합성 불일치 dropId={} {}", dropId, detail)
        }

        return consistencyReportRepository.save(
            ConsistencyReportEntity(
                dropId = dropId,
                totalStock = drop.totalStock,
                confirmedCount = confirmed,
                reservedCount = reserved,
                redisRemaining = redisRemaining,
                consistent = consistent,
                detail = detail,
                checkedAt = clock.instant(),
            ),
        )
    }
}
