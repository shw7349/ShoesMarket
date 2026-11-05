package com.shoesmarket.api.reservation

import com.ninjasquad.springmockk.MockkBean
import com.shoesmarket.api.common.GlobalExceptionHandler
import com.shoesmarket.api.incident.IncidentReporter
import com.shoesmarket.domain.ReservationId
import com.shoesmarket.domain.ReserveResult
import com.shoesmarket.stock.StockReserver
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Instant

@WebMvcTest(ReservationController::class)
@Import(GlobalExceptionHandler::class)
class ReservationControllerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
    ) {
        @MockkBean
        private lateinit var stockReserver: StockReserver

        @MockkBean(relaxed = true)
        private lateinit var incidentReporter: IncidentReporter

        @Test
        fun `선점 성공하면 201과 reservationId를 반환한다`() {
            every { stockReserver.reserve(any(), any()) } returns
                ReserveResult.Success(ReservationId(10), Instant.parse("2026-11-20T11:05:00Z"))

            mockMvc
                .post("/api/v1/drops/1/reservations") {
                    header("X-User-Id", "42")
                }.andExpect {
                    status { isCreated() }
                    jsonPath("$.success") { value(true) }
                    jsonPath("$.data.reservationId") { value(10) }
                }
        }

        @Test
        fun `품절이면 409 SOLD_OUT`() {
            every { stockReserver.reserve(any(), any()) } returns ReserveResult.SoldOut

            mockMvc
                .post("/api/v1/drops/1/reservations") {
                    header("X-User-Id", "42")
                }.andExpect {
                    status { isConflict() }
                    jsonPath("$.error.code") { value("SOLD_OUT") }
                }
        }

        @Test
        fun `토큰 없으면(선점 InvalidToken) 403`() {
            every { stockReserver.reserve(any(), any()) } returns ReserveResult.InvalidToken

            mockMvc
                .post("/api/v1/drops/1/reservations") {
                    header("X-User-Id", "42")
                }.andExpect {
                    status { isForbidden() }
                    jsonPath("$.error.code") { value("INVALID_TOKEN") }
                }
        }

        @Test
        fun `X-User-Id 헤더가 없으면 401`() {
            mockMvc.post("/api/v1/drops/1/reservations").andExpect {
                status { isUnauthorized() }
                jsonPath("$.error.code") { value("UNAUTHORIZED") }
            }
        }
    }
