package com.shoesmarket.stock

import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.ReserveResult
import com.shoesmarket.domain.UserId
import com.shoesmarket.infra.persistence.ReservationRepository
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
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Lua 기반 재고 선점의 동시성 보장을 검증한다.
 * 로컬 redis-server 프로세스를 띄워 실제 Lua EVAL을 실행하고, 예약 DB는 H2를 쓴다.
 * redis-server 바이너리가 없으면 테스트를 스킵한다.
 */
@SpringBootTest
class LuaScriptStockReserverIntegrationTest
    @Autowired
    constructor(
        private val reserver: StockReserver,
        private val redisTemplate: StringRedisTemplate,
        private val reservationRepository: ReservationRepository,
        private val stockInitializer: StockRedisInitializer,
        private val stockProperties: StockProperties,
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
                        ProcessBuilder(
                            "redis-server",
                            "--port",
                            redisPort.toString(),
                            "--save",
                            "",
                            "--appendonly",
                            "no",
                        ).redirectErrorStream(true).start()
                    } catch (e: Exception) {
                        throw TestAbortedException("redis-server 바이너리가 없어 테스트를 스킵한다: ${e.message}")
                    }
                waitUntilReady(redisPort)
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

            private fun waitUntilReady(port: Int) {
                val deadline = System.currentTimeMillis() + 10_000
                while (System.currentTimeMillis() < deadline) {
                    try {
                        Socket("localhost", port).close()
                        return
                    } catch (e: Exception) {
                        Thread.sleep(100)
                    }
                }
                throw IllegalStateException("redis-server가 $port 포트에서 준비되지 않았다")
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
            reservationRepository.deleteAll()
            stockProperties.tokenCheckEnabled = false
        }

        private fun reserveConcurrently(
            dropId: DropId,
            userIds: List<UserId>,
        ): List<ReserveResult> {
            val pool = Executors.newFixedThreadPool(64)
            val ready = CountDownLatch(userIds.size)
            val start = CountDownLatch(1)
            val results = ConcurrentLinkedQueue<ReserveResult>()
            userIds.forEach { userId ->
                pool.submit {
                    ready.countDown()
                    start.await()
                    results.add(reserver.reserve(dropId, userId))
                }
            }
            ready.await(10, TimeUnit.SECONDS)
            start.countDown()
            pool.shutdown()
            pool.awaitTermination(60, TimeUnit.SECONDS)
            return results.toList()
        }

        @Test
        fun `재고 100개에 서로 다른 유저 1000명이 동시 선점하면 정확히 100건만 성공하고 재고는 0이 된다`() {
            val dropId = DropId(1)
            stockInitializer.initialize(dropId, 100)

            val results = reserveConcurrently(dropId, (1..1000L).map { UserId(it) })

            val success = results.count { it is ReserveResult.Success }
            val soldOut = results.count { it is ReserveResult.SoldOut }
            assertThat(success).isEqualTo(100)
            assertThat(soldOut).isEqualTo(900)
            assertThat(redisTemplate.opsForValue().get(StockKeys.stock(dropId))).isEqualTo("0")
            assertThat(redisTemplate.opsForSet().size(StockKeys.buyers(dropId))).isEqualTo(100)
            assertThat(reservationRepository.count()).isEqualTo(100)
        }

        @Test
        fun `같은 유저가 10번 동시에 선점하면 1건만 성공한다`() {
            val dropId = DropId(2)
            stockInitializer.initialize(dropId, 100)

            val results = reserveConcurrently(dropId, List(10) { UserId(500) })

            assertThat(results.count { it is ReserveResult.Success }).isEqualTo(1)
            assertThat(results.count { it is ReserveResult.AlreadyPurchased }).isEqualTo(9)
            assertThat(reservationRepository.count()).isEqualTo(1)
        }

        @Test
        fun `토큰 검증이 켜진 상태에서 토큰 없이 선점하면 InvalidToken을 반환한다`() {
            val dropId = DropId(3)
            stockInitializer.initialize(dropId, 100)
            stockProperties.tokenCheckEnabled = true

            val result = reserver.reserve(dropId, UserId(1))

            assertThat(result).isEqualTo(ReserveResult.InvalidToken)
        }

        @Test
        fun `재고가 초기화되지 않은(미오픈) 드롭은 DropNotOpen을 반환한다`() {
            val result = reserver.reserve(DropId(999), UserId(1))

            assertThat(result).isEqualTo(ReserveResult.DropNotOpen)
        }
    }
