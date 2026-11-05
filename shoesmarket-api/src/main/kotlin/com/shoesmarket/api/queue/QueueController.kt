package com.shoesmarket.api.queue

import com.shoesmarket.api.common.ApiException
import com.shoesmarket.api.common.ApiResponse
import com.shoesmarket.api.common.ErrorCode
import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.QueueEnterResult
import com.shoesmarket.domain.UserId
import com.shoesmarket.queue.QueueMembership
import com.shoesmarket.queue.QueueService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Tag(name = "Queue", description = "대기열 API")
@RestController
class QueueController(
    private val queueService: QueueService,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val sseScheduler = Executors.newScheduledThreadPool(4)

    @Operation(summary = "대기열 진입")
    @PostMapping("/api/v1/drops/{dropId}/queue")
    fun enter(
        @PathVariable dropId: Long,
        @RequestHeader(value = "X-User-Id", required = false) userIdHeader: Long?,
    ): ApiResponse<QueueEnterResponse> {
        val userId = userIdHeader?.let { UserId(it) } ?: throw ApiException(ErrorCode.UNAUTHORIZED)
        return when (val result = queueService.enter(DropId(dropId), userId)) {
            is QueueEnterResult.Waiting -> ApiResponse.success(QueueEnterResponse(result.rank))
            QueueEnterResult.AlreadyInQueue -> throw ApiException(ErrorCode.ALREADY_IN_QUEUE)
            QueueEnterResult.DropNotOpen -> throw ApiException(ErrorCode.DROP_NOT_OPEN)
        }
    }

    @Operation(summary = "내 순번/입장 여부 조회")
    @GetMapping("/api/v1/drops/{dropId}/queue/me")
    fun me(
        @PathVariable dropId: Long,
        @RequestHeader(value = "X-User-Id", required = false) userIdHeader: Long?,
    ): ApiResponse<QueueStatusResponse> {
        val userId = userIdHeader?.let { UserId(it) } ?: throw ApiException(ErrorCode.UNAUTHORIZED)
        val status = queueService.status(DropId(dropId), userId)
        return ApiResponse.success(QueueStatusResponse(status.membership.name, status.rank))
    }

    @Operation(summary = "순번 SSE 스트림", description = "2초 간격으로 순번을 push하고 입장 시 종료한다.")
    @GetMapping("/api/v1/drops/{dropId}/queue/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(
        @PathVariable dropId: Long,
        @RequestHeader(value = "X-User-Id", required = false) userIdHeader: Long?,
    ): SseEmitter {
        val userId = userIdHeader?.let { UserId(it) } ?: throw ApiException(ErrorCode.UNAUTHORIZED)
        val emitter = SseEmitter(TimeUnit.MINUTES.toMillis(10))
        val future =
            sseScheduler.scheduleAtFixedRate({
                try {
                    val status = queueService.status(DropId(dropId), userId)
                    emitter.send(
                        SseEmitter
                            .event()
                            .name(status.membership.name)
                            .data(QueueStatusResponse(status.membership.name, status.rank)),
                    )
                    if (status.membership == QueueMembership.ADMITTED) {
                        emitter.complete()
                    }
                } catch (e: Exception) {
                    log.debug("SSE 전송 종료: {}", e.message)
                    emitter.completeWithError(e)
                }
            }, 0, 2, TimeUnit.SECONDS)
        emitter.onCompletion { future.cancel(true) }
        emitter.onTimeout {
            future.cancel(true)
            emitter.complete()
        }
        return emitter
    }
}

data class QueueEnterResponse(
    val rank: Long,
)

data class QueueStatusResponse(
    val status: String,
    val rank: Long?,
)
