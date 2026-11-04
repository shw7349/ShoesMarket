package com.shoesmarket.api.incident

import com.shoesmarket.infra.persistence.IncidentEntity
import com.shoesmarket.infra.persistence.IncidentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

/**
 * 장애를 기록한다. 항상 DB에 적재하고, 원인을 분석해 Notion 하위페이지로도 남긴다(설정 시).
 */
@Component
class IncidentReporter(
    private val incidentRepository: IncidentRepository,
    private val analyzer: IncidentAnalyzer,
    private val notionRecorder: IncidentNotionRecorder,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun report(
        source: String,
        throwable: Throwable,
        context: Map<String, String> = emptyMap(),
    ): IncidentEntity {
        val analysis = analyzer.analyze(throwable)
        val entity =
            IncidentEntity(
                category = analysis.category,
                source = source,
                message = (throwable.message ?: throwable.javaClass.simpleName).take(1000),
                probableCause = analysis.probableCause,
                suggestedAction = analysis.suggestedAction,
                context = context.entries.joinToString(", ") { "${it.key}=${it.value}" }.take(2000),
                occurredAt = clock.instant(),
            )
        val saved = incidentRepository.save(entity)
        log.warn(
            "장애 기록 id={} category={} source={} cause={}",
            saved.id,
            saved.category,
            saved.source,
            saved.probableCause,
        )

        runCatching { notionRecorder.record(saved) }
            .getOrNull()
            ?.let { url ->
                saved.linkNotion(url)
                incidentRepository.save(saved)
            }

        return saved
    }
}
