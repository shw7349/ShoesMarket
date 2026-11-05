package com.shoesmarket.batch

import com.shoesmarket.domain.ReservationStatus
import com.shoesmarket.infra.persistence.ConsistencyReportEntity
import com.shoesmarket.infra.persistence.ConsistencyReportRepository
import com.shoesmarket.infra.persistence.DropEventEntity
import com.shoesmarket.infra.persistence.DropEventRepository
import com.shoesmarket.infra.persistence.ReservationRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Optional

class StockConsistencyServiceTest :
    DescribeSpec({
        val reservationRepository = mockk<ReservationRepository>()
        val dropEventRepository = mockk<DropEventRepository>()
        val reportRepository = mockk<ConsistencyReportRepository>()
        val redisTemplate = mockk<StringRedisTemplate>()
        val valueOps = mockk<ValueOperations<String, String>>()
        val clock = Clock.fixed(Instant.parse("2026-11-20T12:30:00Z"), ZoneOffset.UTC)
        val service = StockConsistencyService(reservationRepository, dropEventRepository, reportRepository, redisTemplate, clock)

        beforeEach {
            clearMocks(reservationRepository, dropEventRepository, reportRepository, redisTemplate, valueOps)
            every { dropEventRepository.findById(1) } returns
                Optional.of(DropEventEntity(1, 100, 1, Instant.parse("2026-11-20T11:00:00Z"), Instant.parse("2026-11-20T12:00:00Z")))
            every { redisTemplate.opsForValue() } returns valueOps
        }

        describe("재고 정합성 검증") {
            it("confirmed + reserved + redis == total이면 consistent=true로 기록한다") {
                every { reservationRepository.countByDropIdAndStatus(1, ReservationStatus.CONFIRMED) } returns 60
                every { reservationRepository.countByDropIdAndStatus(1, ReservationStatus.RESERVED) } returns 10
                every { valueOps.get(any()) } returns "30"
                val saved = slot<ConsistencyReportEntity>()
                every { reportRepository.save(capture(saved)) } answers { saved.captured }

                service.check(1)

                saved.captured.consistent shouldBe true
            }

            it("합이 total과 다르면 consistent=false로 기록한다") {
                every { reservationRepository.countByDropIdAndStatus(1, ReservationStatus.CONFIRMED) } returns 60
                every { reservationRepository.countByDropIdAndStatus(1, ReservationStatus.RESERVED) } returns 10
                every { valueOps.get(any()) } returns "25" // 합 95 != 100
                val saved = slot<ConsistencyReportEntity>()
                every { reportRepository.save(capture(saved)) } answers { saved.captured }

                service.check(1)

                saved.captured.consistent shouldBe false
            }
        }
    })
