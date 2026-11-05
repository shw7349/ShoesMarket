package com.shoesmarket.batch

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 결제 대사 스케줄러 (명세 8장, 10분 주기).
 */
@Component
class PaymentReconciliationScheduler(
    private val paymentReconciliationService: PaymentReconciliationService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${shoesmarket.batch.reconcile-interval-ms:600000}")
    fun run() {
        val summary = paymentReconciliationService.reconcile()
        if (summary.scanned > 0) {
            log.info("결제 대사 완료 scanned={} recovered={} failed={}", summary.scanned, summary.recovered, summary.failed)
        }
    }
}
