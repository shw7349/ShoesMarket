package com.shoesmarket.stock

import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.ReservationId
import com.shoesmarket.domain.ReserveResult
import com.shoesmarket.domain.UserId
import com.shoesmarket.infra.persistence.ReservationEntity
import com.shoesmarket.infra.persistence.ReservationRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.time.Clock

/**
 * Redis Lua 스크립트로 재고를 원자적으로 선점한다 (명세 5.3, 7.1). 기본 전략.
 * Lua가 "토큰 확인 + 1인 제한 + 재고 차감"을 한 번에 처리해 경합 비용을 줄인다.
 */
@Component
@ConditionalOnProperty(name = ["shoesmarket.stock.strategy"], havingValue = "LUA", matchIfMissing = true)
class LuaScriptStockReserver(
    private val redisTemplate: StringRedisTemplate,
    @Qualifier("reserveStockScript") private val reserveScript: RedisScript<Long>,
    @Qualifier("releaseStockScript") private val releaseScript: RedisScript<Long>,
    private val reservationRepository: ReservationRepository,
    private val stockProperties: StockProperties,
    private val reservationProperties: ReservationProperties,
    private val clock: Clock,
) : StockReserver {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun reserve(
        dropId: DropId,
        userId: UserId,
    ): ReserveResult {
        val tokenRequired = if (stockProperties.tokenCheckEnabled) "1" else "0"
        val keys = listOf(StockKeys.stock(dropId), StockKeys.buyers(dropId), StockKeys.token(dropId, userId))

        val result = redisTemplate.execute(reserveScript, keys, userId.value.toString(), tokenRequired)
        if (result == null) {
            return ReserveResult.DropNotOpen
        }

        return when {
            result >= 0 -> insertReservation(dropId, userId)
            result == -1L -> ReserveResult.SoldOut
            result == -2L -> ReserveResult.AlreadyPurchased
            result == -3L -> ReserveResult.InvalidToken
            else -> ReserveResult.DropNotOpen // -4
        }
    }

    private fun insertReservation(
        dropId: DropId,
        userId: UserId,
    ): ReserveResult {
        val expiresAt = clock.instant().plusSeconds(reservationProperties.ttlSeconds)
        return try {
            val saved =
                reservationRepository.save(
                    ReservationEntity(dropId = dropId.value, userId = userId.value, expiresAt = expiresAt),
                )
            ReserveResult.Success(ReservationId(saved.id), expiresAt)
        } catch (e: DataIntegrityViolationException) {
            // DB 유니크 제약(최후 방어선) 위반 → Redis 선점을 보상하고 이미 구매로 처리
            log.warn("선점 DB INSERT 실패, Redis 보상 실행 dropId={} userId={}", dropId.value, userId.value, e)
            redisTemplate.execute(
                releaseScript,
                listOf(StockKeys.stock(dropId), StockKeys.buyers(dropId)),
                userId.value.toString(),
            )
            ReserveResult.AlreadyPurchased
        }
    }
}
