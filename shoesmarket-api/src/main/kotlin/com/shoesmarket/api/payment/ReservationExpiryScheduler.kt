package com.shoesmarket.api.payment

import com.shoesmarket.domain.DropId
import com.shoesmarket.domain.ReservationStatus
import com.shoesmarket.domain.UserId
import com.shoesmarket.infra.persistence.ReservationRepository
import com.shoesmarket.stock.StockReleaser
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

/**
 * 선점 만료 스케줄러 (명세 7.3). 만료 대상을 조건부 UPDATE로 EXPIRED 전이시키고,
 * 영향 행이 1일 때만 재고를 반환해 결제 확정과의 경합에서 이중 반환을 막는다.
 */
@Component
class ReservationExpiryScheduler(
    private val reservationRepository: ReservationRepository,
    private val stockReleaser: StockReleaser,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${shoesmarket.reservation.expiry-interval-ms:10000}")
    @Transactional
    fun run() {
        val expired =
            reservationRepository.findTop100ByStatusAndExpiresAtBefore(ReservationStatus.RESERVED, clock.instant())
        expired.forEach { reservation ->
            if (reservationRepository.expireIfReserved(reservation.id) == 1) {
                stockReleaser.release(DropId(reservation.dropId), UserId(reservation.userId))
                log.info("선점 만료 및 재고 반환 reservationId={} dropId={}", reservation.id, reservation.dropId)
            }
        }
    }
}
