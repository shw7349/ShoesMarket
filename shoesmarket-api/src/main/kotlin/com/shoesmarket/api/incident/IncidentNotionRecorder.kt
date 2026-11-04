package com.shoesmarket.api.incident

import com.shoesmarket.infra.persistence.IncidentEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * 장애를 Notion 하위페이지로 기록한다. 실패해도 예외를 전파하지 않는다(장애 기록이 2차 장애가 되면 안 됨).
 */
interface IncidentNotionRecorder {
    /** 생성된 Notion 페이지 URL을 반환한다. 비활성/실패 시 null. */
    fun record(incident: IncidentEntity): String?
}

@Component
class NotionIncidentRecorder(
    private val properties: NotionProperties,
) : IncidentNotionRecorder {
    private val log = LoggerFactory.getLogger(javaClass)

    private val restClient: RestClient by lazy {
        RestClient.builder().baseUrl("https://api.notion.com/v1").build()
    }

    override fun record(incident: IncidentEntity): String? {
        if (!properties.isUsable()) {
            log.debug("Notion 장애 기록 비활성화 상태 — DB 적재만 수행")
            return null
        }
        return try {
            val body =
                mapOf(
                    "parent" to mapOf("type" to "page_id", "page_id" to properties.parentPageId),
                    "properties" to
                        mapOf(
                            "title" to
                                mapOf(
                                    "title" to
                                        listOf(
                                            mapOf("text" to mapOf("content" to "[${incident.category}] ${incident.message.take(80)}")),
                                        ),
                                ),
                        ),
                    "children" to
                        listOf(
                            paragraph("발생 위치: ${incident.source}"),
                            paragraph("발생 시각: ${incident.occurredAt}"),
                            heading("원인 분석"),
                            paragraph(incident.probableCause),
                            heading("조치 방법"),
                            paragraph(incident.suggestedAction),
                            heading("컨텍스트"),
                            paragraph(incident.context.ifBlank { "(없음)" }),
                        ),
                )

            val response =
                restClient
                    .post()
                    .uri("/pages")
                    .header("Authorization", "Bearer ${properties.token}")
                    .header("Notion-Version", properties.version)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(Map::class.java)

            (response?.get("url") as? String).also { log.info("장애를 Notion에 기록: {}", it) }
        } catch (e: Exception) {
            log.warn("Notion 장애 기록 실패(무시하고 계속): {}", e.message)
            null
        }
    }

    private fun heading(text: String): Map<String, Any> =
        mapOf(
            "object" to "block",
            "type" to "heading_3",
            "heading_3" to mapOf("rich_text" to listOf(mapOf("text" to mapOf("content" to text)))),
        )

    private fun paragraph(text: String): Map<String, Any> =
        mapOf(
            "object" to "block",
            "type" to "paragraph",
            "paragraph" to mapOf("rich_text" to listOf(mapOf("text" to mapOf("content" to text)))),
        )
}
