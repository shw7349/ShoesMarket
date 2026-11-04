package com.shoesmarket.api.incident

import com.shoesmarket.domain.IncidentCategory
import com.shoesmarket.domain.InvalidStatusTransitionException
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.redis.RedisConnectionFailureException

class IncidentAnalyzerTest :
    DescribeSpec({
        val analyzer = IncidentAnalyzer()

        describe("장애 원인 분석") {
            it("DB 제약 위반은 DATA_INTEGRITY로 분류하고 조치를 제시한다") {
                val analysis = analyzer.analyze(DataIntegrityViolationException("duplicate"))
                analysis.category shouldBe IncidentCategory.DATA_INTEGRITY
                analysis.suggestedAction shouldContain "멱등"
            }

            it("Redis 연결 실패는 REDIS_FAILURE로 분류한다") {
                val analysis = analyzer.analyze(RedisConnectionFailureException("down"))
                analysis.category shouldBe IncidentCategory.REDIS_FAILURE
            }

            it("상태 전이 예외는 STATUS_TRANSITION으로 분류한다") {
                val analysis = analyzer.analyze(InvalidStatusTransitionException("RESERVED -> RESERVED"))
                analysis.category shouldBe IncidentCategory.STATUS_TRANSITION
            }

            it("미분류 예외는 UNKNOWN으로 분류한다") {
                val analysis = analyzer.analyze(RuntimeException("뭔가 터짐"))
                analysis.category shouldBe IncidentCategory.UNKNOWN
            }
        }
    })
