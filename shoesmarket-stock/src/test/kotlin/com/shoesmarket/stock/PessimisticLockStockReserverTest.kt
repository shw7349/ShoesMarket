package com.shoesmarket.stock

import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.ReserveResult
import com.shoesmarket.domain.UserId
import com.shoesmarket.infra.persistence.DropStockEntity
import com.shoesmarket.infra.persistence.DropStockRepository
import com.shoesmarket.infra.persistence.ReservationRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.opentest4j.TestAbortedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * 비관적 락(FOR UPDATE) 비교 구현의 초과판매 방지 검증 (명세 10.1).
 * 전략을 PESSIMISTIC으로 두고 서로 다른 유저가 동시에 선점해도 재고만큼만 성공하는지 본다.
 */
@SpringBootTest
@TestPropertySource(properties = ["shoesmarket.stock.strategy=PESSIMISTIC"])
class PessimisticLockStockReserverTest
    @Autowired
    constructor(
        private val reserver: StockReserver,
        private val dropStockRepository: DropStockRepository,
        private val reservationRepository: ReservationRepository,
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

        @Test
        fun `재고 100에 서로 다른 유저 300명이 동시 선점하면 정확히 100건만 성공한다`() {
            val dropId = DropId(1)
            dropStockRepository.save(DropStockEntity(dropId = dropId.value, remaining = 100))

            val pool = Executors.newFixedThreadPool(32)
            val start = CountDownLatch(1)
            val results = ConcurrentLinkedQueue<ReserveResult>()
            (1..300L).forEach { id ->
                pool.submit {
                    start.await()
                    results.add(reserver.reserve(dropId, UserId(id)))
                }
            }
            start.countDown()
            pool.shutdown()
            pool.awaitTermination(120, TimeUnit.SECONDS)

            assertThat(results.count { it is ReserveResult.Success }).isEqualTo(100)
            assertThat(dropStockRepository.findById(dropId.value).get().remaining).isEqualTo(0)
            assertThat(reservationRepository.count()).isEqualTo(100)
        }
    }
