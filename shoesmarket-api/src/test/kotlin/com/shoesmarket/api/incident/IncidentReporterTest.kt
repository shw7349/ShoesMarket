package com.shoesmarket.api.incident

import com.shoesmarket.domain.IncidentCategory
import com.shoesmarket.infra.persistence.IncidentEntity
import com.shoesmarket.infra.persistence.IncidentRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.dao.DataIntegrityViolationException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/**
 * 장애 전파 시 원인 분석 결과를 DB에 적재하고 Notion 하위페이지로 자동 기록하는 흐름의 예시 테스트.
 */
class IncidentReporterTest :
    DescribeSpec({
        val incidentRepository = mockk<IncidentRepository>()
        val notionRecorder = mockk<IncidentNotionRecorder>()
        val clock = Clock.fixed(Instant.parse("2026-11-20T11:03:00Z"), ZoneOffset.UTC)
        val reporter = IncidentReporter(incidentRepository, IncidentAnalyzer(), notionRecorder, clock)

        describe("장애 기록") {
            it("중복 선점 장애를 분석해 DB에 적재하고 Notion 페이지 URL을 연결한다") {
                val saved = slot<IncidentEntity>()
                every { incidentRepository.save(capture(saved)) } answers { saved.captured }
                every { notionRecorder.record(any()) } returns "https://notion.so/incident-page"

                val incident =
                    reporter.report(
                        source = "POST /api/v1/drops/1/reservations",
                        throwable = DataIntegrityViolationException("Duplicate entry for uk_reservation_drop_user"),
                        context = mapOf("dropId" to "1", "userId" to "42"),
                    )

                incident.category shouldBe IncidentCategory.DATA_INTEGRITY
                incident.source shouldBe "POST /api/v1/drops/1/reservations"
                incident.context shouldBe "dropId=1, userId=42"
                incident.occurredAt shouldBe Instant.parse("2026-11-20T11:03:00Z")
                incident.notionPageUrl shouldBe "https://notion.so/incident-page"

                // Notion 하위페이지 자동 기록이 호출됐고, DB에는 2번(최초 적재 + URL 연결) 저장된다.
                verify(exactly = 1) { notionRecorder.record(any()) }
                verify(exactly = 2) { incidentRepository.save(any()) }
            }

            it("Notion 기록이 비활성(null)이어도 DB 적재는 성공한다") {
                every { incidentRepository.save(any()) } answers { firstArg() }
                every { notionRecorder.record(any()) } returns null

                val incident = reporter.report("batch:stockConsistency", RuntimeException("mismatch"))

                incident.category shouldBe IncidentCategory.UNKNOWN
                incident.notionPageUrl shouldBe null
            }
        }
    })
