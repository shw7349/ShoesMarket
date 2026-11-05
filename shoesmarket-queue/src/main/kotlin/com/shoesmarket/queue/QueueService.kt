package com.shoesmarket.queue

import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.DropStatus
import com.shoesmarket.domain.QueueEnterResult
import com.shoesmarket.domain.UserId
import com.shoesmarket.infra.persistence.DropEventRepository
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Duration

enum class QueueMembership { WAITING, ADMITTED, NOT_IN_QUEUE }

data class QueueStatus(
    val membership: QueueMembership,
    val rank: Long?,
)

/**
 * Redis Sorted Set 기반 대기열 (명세 5.2).
 * 진입은 ZADD NX(동일 유저 중복 방지), 순번은 ZRANK+1, 입장은 ZPOPMIN 후 토큰 발급.
 */
@Service
class QueueService(
    private val redisTemplate: StringRedisTemplate,
    private val dropEventRepository: DropEventRepository,
    private val queueProperties: QueueProperties,
    private val clock: Clock,
) {
    fun enter(
        dropId: DropId,
        userId: UserId,
    ): QueueEnterResult {
        val drop = dropEventRepository.findById(dropId.value).orElse(null) ?: return QueueEnterResult.DropNotOpen
        if (drop.status != DropStatus.OPEN) {
            return QueueEnterResult.DropNotOpen
        }

        val member = userId.value.toString()
        val added =
            redisTemplate.opsForZSet().addIfAbsent(QueueKeys.queue(dropId), member, clock.millis().toDouble())
        if (added != true) {
            return QueueEnterResult.AlreadyInQueue
        }
        val rank = (redisTemplate.opsForZSet().rank(QueueKeys.queue(dropId), member) ?: 0L) + 1
        return QueueEnterResult.Waiting(rank)
    }

    fun status(
        dropId: DropId,
        userId: UserId,
    ): QueueStatus {
        val member = userId.value.toString()
        if (redisTemplate.hasKey(QueueKeys.token(dropId, member))) {
            return QueueStatus(QueueMembership.ADMITTED, null)
        }
        val rank = redisTemplate.opsForZSet().rank(QueueKeys.queue(dropId), member)
        return if (rank == null) {
            QueueStatus(QueueMembership.NOT_IN_QUEUE, null)
        } else {
            QueueStatus(QueueMembership.WAITING, rank + 1)
        }
    }

    /**
     * 상위 [count]명을 입장시키고 토큰을 발급한다. 입장한 userId 목록을 반환한다.
     */
    fun admit(
        dropId: DropId,
        count: Int,
    ): List<String> {
        if (count <= 0) return emptyList()
        val popped = redisTemplate.opsForZSet().popMin(QueueKeys.queue(dropId), count.toLong()) ?: return emptyList()
        val ttl = Duration.ofSeconds(queueProperties.tokenTtlSeconds)
        return popped.mapNotNull { it.value }.onEach { member ->
            redisTemplate.opsForValue().set(QueueKeys.token(dropId, member), "1", ttl)
        }
    }
}
