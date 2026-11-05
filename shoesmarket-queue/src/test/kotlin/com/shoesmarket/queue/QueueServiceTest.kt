package com.shoesmarket.queue

import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.DropStatus
import com.shoesmarket.domain.QueueEnterResult
import com.shoesmarket.domain.UserId
import com.shoesmarket.infra.persistence.DropEventEntity
import com.shoesmarket.infra.persistence.DropEventRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opentest4j.TestAbortedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.net.ServerSocket
import java.net.Socket
import java.time.Instant

@SpringBootTest
class QueueServiceTest
    @Autowired
    constructor(
        private val queueService: QueueService,
        private val redisTemplate: StringRedisTemplate,
        private val dropEventRepository: DropEventRepository,
    ) {
        companion object {
            private var redisProcess: Process? = null
            private var redisPort: Int = 0

            @BeforeAll
            @JvmStatic
            fun startRedis() {
                redisPort = ServerSocket(0).use { it.localPort }
                redisProcess =
                    try {
                        ProcessBuilder("redis-server", "--port", redisPort.toString(), "--save", "", "--appendonly", "no")
                            .redirectErrorStream(true)
                            .start()
                    } catch (e: Exception) {
                        throw TestAbortedException("redis-server 바이너리가 없어 테스트를 스킵한다: ${e.message}")
                    }
                val deadline = System.currentTimeMillis() + 10_000
                while (System.currentTimeMillis() < deadline) {
                    try {
                        Socket("localhost", redisPort).close()
                        return
                    } catch (e: Exception) {
                        Thread.sleep(100)
                    }
                }
                throw IllegalStateException("redis-server가 준비되지 않았다")
            }

            @AfterAll
            @JvmStatic
            fun stopRedis() {
                redisProcess?.destroyForcibly()
            }

            @JvmStatic
            @DynamicPropertySource
            fun redisProps(registry: DynamicPropertyRegistry) {
                registry.add("spring.data.redis.host") { "localhost" }
                registry.add("spring.data.redis.port") { redisPort }
            }
        }

        @BeforeEach
        fun cleanup() {
            val connection = redisTemplate.connectionFactory!!.connection
            try {
                connection.serverCommands().flushDb()
            } finally {
                connection.close()
            }
            dropEventRepository.deleteAll()
        }

        private fun saveDrop(status: DropStatus): Long {
            val drop =
                DropEventEntity(
                    productId = 1L,
                    totalStock = 500,
                    perUserLimit = 1,
                    openAt = Instant.parse("2026-11-20T11:00:00Z"),
                    closeAt = Instant.parse("2026-11-20T12:00:00Z"),
                    status = status,
                )
            return dropEventRepository.save(drop).id
        }

        @Test
        fun `같은 유저가 중복 진입하면 AlreadyInQueue를 반환한다`() {
            val dropId = DropId(saveDrop(DropStatus.OPEN))

            val first = queueService.enter(dropId, UserId(1))
            val second = queueService.enter(dropId, UserId(1))

            assertThat(first).isInstanceOf(QueueEnterResult.Waiting::class.java)
            assertThat(second).isEqualTo(QueueEnterResult.AlreadyInQueue)
        }

        @Test
        fun `진입 순서대로 순번을 부여한다`() {
            val dropId = DropId(saveDrop(DropStatus.OPEN))

            val r1 = queueService.enter(dropId, UserId(1)) as QueueEnterResult.Waiting
            val r2 = queueService.enter(dropId, UserId(2)) as QueueEnterResult.Waiting
            val r3 = queueService.enter(dropId, UserId(3)) as QueueEnterResult.Waiting

            assertThat(listOf(r1.rank, r2.rank, r3.rank)).containsExactly(1L, 2L, 3L)
        }

        @Test
        fun `입장 스케줄러가 상위 N명에게 토큰을 발급한다`() {
            val dropId = DropId(saveDrop(DropStatus.OPEN))
            (1L..5L).forEach { queueService.enter(dropId, UserId(it)) }

            val admitted = queueService.admit(dropId, 2)

            assertThat(admitted).containsExactly("1", "2")
            assertThat(redisTemplate.hasKey(QueueKeys.token(dropId, "1"))).isTrue()
            assertThat(queueService.status(dropId, UserId(1)).membership).isEqualTo(QueueMembership.ADMITTED)
            assertThat(queueService.status(dropId, UserId(3)).membership).isEqualTo(QueueMembership.WAITING)
            assertThat(queueService.status(dropId, UserId(3)).rank).isEqualTo(1L)
        }

        @Test
        fun `오픈 상태가 아니면 DropNotOpen을 반환한다`() {
            val dropId = DropId(saveDrop(DropStatus.SCHEDULED))

            assertThat(queueService.enter(dropId, UserId(1))).isEqualTo(QueueEnterResult.DropNotOpen)
        }
    }
