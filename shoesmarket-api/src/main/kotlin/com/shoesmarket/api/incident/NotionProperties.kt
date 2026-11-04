package com.shoesmarket.api.incident

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 장애를 Notion 하위페이지로 자동 기록하기 위한 설정.
 * 토큰과 부모 페이지 ID는 환경변수로 주입하며, 비어 있으면 Notion 기록은 건너뛴다(DB 적재는 항상 수행).
 */
@ConfigurationProperties(prefix = "shoesmarket.notion")
class NotionProperties {
    var enabled: Boolean = false

    /** Notion 통합(internal integration) 토큰. 환경변수 NOTION_TOKEN 등으로 주입. */
    var token: String = ""

    /** 장애 기록 하위페이지들을 생성할 부모 페이지 ID. */
    var parentPageId: String = ""

    var version: String = "2022-06-28"

    fun isUsable(): Boolean = enabled && token.isNotBlank() && parentPageId.isNotBlank()
}
