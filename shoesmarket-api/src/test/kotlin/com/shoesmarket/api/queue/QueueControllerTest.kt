package com.shoesmarket.api.queue

import com.ninjasquad.springmockk.MockkBean
import com.shoesmarket.api.common.GlobalExceptionHandler
import com.shoesmarket.api.incident.IncidentReporter
import com.shoesmarket.api.web.WebSession
import com.shoesmarket.domain.QueueEnterResult
import com.shoesmarket.queue.QueueService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(QueueController::class)
@Import(GlobalExceptionHandler::class)
class QueueControllerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
    ) {
        @MockkBean
        private lateinit var queueService: QueueService

        @MockkBean(relaxed = true)
        private lateinit var incidentReporter: IncidentReporter

        @MockkBean(relaxed = true)
        private lateinit var webSession: WebSession

        @Test
        fun `진입하면 200과 순번을 반환한다`() {
            every { queueService.enter(any(), any()) } returns QueueEnterResult.Waiting(7)

            mockMvc.post("/api/v1/drops/1/queue") { header("X-User-Id", "42") }.andExpect {
                status { isOk() }
                jsonPath("$.data.rank") { value(7) }
            }
        }

        @Test
        fun `중복 진입이면 409 ALREADY_IN_QUEUE`() {
            every { queueService.enter(any(), any()) } returns QueueEnterResult.AlreadyInQueue

            mockMvc.post("/api/v1/drops/1/queue") { header("X-User-Id", "42") }.andExpect {
                status { isConflict() }
                jsonPath("$.error.code") { value("ALREADY_IN_QUEUE") }
            }
        }

        @Test
        fun `오픈 전이면 400 DROP_NOT_OPEN`() {
            every { queueService.enter(any(), any()) } returns QueueEnterResult.DropNotOpen

            mockMvc.post("/api/v1/drops/1/queue") { header("X-User-Id", "42") }.andExpect {
                status { isBadRequest() }
                jsonPath("$.error.code") { value("DROP_NOT_OPEN") }
            }
        }

        @Test
        fun `헤더 없으면 401`() {
            mockMvc.post("/api/v1/drops/1/queue").andExpect {
                status { isUnauthorized() }
            }
        }
    }
