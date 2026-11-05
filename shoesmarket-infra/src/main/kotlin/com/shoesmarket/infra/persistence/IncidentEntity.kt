package com.shoesmarket.infra.persistence

import com.shoesmarket.domain.IncidentCategory
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "incident")
class IncidentEntity(
    category: IncidentCategory,
    source: String,
    message: String,
    probableCause: String,
    suggestedAction: String,
    context: String,
    occurredAt: Instant,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    var category: IncidentCategory = category
        protected set

    @Column(name = "source", nullable = false, length = 200)
    var source: String = source
        protected set

    @Column(name = "message", nullable = false, length = 1000)
    var message: String = message
        protected set

    @Column(name = "probable_cause", nullable = false, length = 1000)
    var probableCause: String = probableCause
        protected set

    @Column(name = "suggested_action", nullable = false, length = 1000)
    var suggestedAction: String = suggestedAction
        protected set

    @Column(name = "context", nullable = false, length = 2000)
    var context: String = context
        protected set

    @Column(name = "notion_page_url", length = 500)
    var notionPageUrl: String? = null
        protected set

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant = occurredAt
        protected set

    fun linkNotion(url: String) {
        this.notionPageUrl = url
    }
}
