package com.shoesmarket.infra.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "review")
class ReviewEntity(
    productId: Long,
    memberId: Long,
    authorName: String,
    rating: Int,
    content: String,
    imageUrl: String? = null,
) : BaseTimeEntity() {
    init {
        require(rating in 1..5) { "평점은 1~5 사이여야 합니다." }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(name = "product_id", nullable = false)
    var productId: Long = productId
        protected set

    @Column(name = "member_id", nullable = false)
    var memberId: Long = memberId
        protected set

    @Column(name = "author_name", nullable = false, length = 100)
    var authorName: String = authorName
        protected set

    @Column(name = "rating", nullable = false)
    var rating: Int = rating
        protected set

    @Column(name = "content", nullable = false, length = 2000)
    var content: String = content
        protected set

    @Column(name = "image_url", length = 500)
    var imageUrl: String? = imageUrl
        protected set

    @Column(name = "hidden", nullable = false)
    var hidden: Boolean = false
        protected set

    fun edit(
        rating: Int,
        content: String,
        imageUrl: String?,
    ) {
        require(rating in 1..5) { "평점은 1~5 사이여야 합니다." }
        this.rating = rating
        this.content = content
        this.imageUrl = imageUrl
    }

    fun hide() {
        this.hidden = true
    }
}
