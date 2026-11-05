package com.shoesmarket.queue

import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.DropStatus
import com.shoesmarket.infra.persistence.DropEventRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 입장 스케줄러 (명세 5.2). 1초마다 OPEN 드롭의 대기열 상위 N명을 입장시켜 토큰을 발급한다.
 */
@Component
class QueueAdmissionScheduler(
    private val dropEventRepository: DropEventRepository,
    private val queueService: QueueService,
    private val queueProperties: QueueProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${shoesmarket.queue.admit-interval-ms:1000}")
    fun run() {
        dropEventRepository.findByStatus(DropStatus.OPEN).forEach { drop ->
            val admitted = queueService.admit(DropId(drop.id), queueProperties.admitPerSecond)
            if (admitted.isNotEmpty()) {
                log.info("입장 처리 dropId={} 인원={}", drop.id, admitted.size)
            }
        }
    }
}
