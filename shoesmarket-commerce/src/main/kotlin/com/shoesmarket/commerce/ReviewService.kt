package com.shoesmarket.commerce

import com.shoesmarket.infra.persistence.OrderRepository
import com.shoesmarket.infra.persistence.ReviewEntity
import com.shoesmarket.infra.persistence.ReviewRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

class NotPurchasedException : RuntimeException("구매한 상품에만 후기를 작성할 수 있습니다.")

class ReviewForbiddenException : RuntimeException("본인이 작성한 후기만 수정/삭제할 수 있습니다.")

class ReviewNotFoundException : RuntimeException("후기를 찾을 수 없습니다.")

data class ReviewSummary(
    val average: Double,
    val count: Long,
)

enum class ReviewSort { LATEST, RATING }

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val orderRepository: OrderRepository,
) {
    @Transactional
    fun write(
        productId: Long,
        memberId: Long,
        authorName: String,
        rating: Int,
        content: String,
        imageUrl: String?,
    ): ReviewEntity {
        if (!orderRepository.hasPurchasedProduct(memberId, productId)) {
            throw NotPurchasedException()
        }
        return reviewRepository.save(
            ReviewEntity(
                productId = productId,
                memberId = memberId,
                authorName = authorName,
                rating = rating,
                content = content,
                imageUrl = imageUrl,
            ),
        )
    }

    @Transactional
    fun update(
        reviewId: Long,
        memberId: Long,
        rating: Int,
        content: String,
        imageUrl: String?,
    ): ReviewEntity {
        val review = reviewRepository.findById(reviewId).orElseThrow { ReviewNotFoundException() }
        if (review.memberId != memberId) throw ReviewForbiddenException()
        review.edit(rating, content, imageUrl)
        return review
    }

    @Transactional
    fun delete(
        reviewId: Long,
        memberId: Long,
    ) {
        val review = reviewRepository.findById(reviewId).orElseThrow { ReviewNotFoundException() }
        if (review.memberId != memberId) throw ReviewForbiddenException()
        reviewRepository.delete(review)
    }

    @Transactional
    fun hide(reviewId: Long) {
        val review = reviewRepository.findById(reviewId).orElseThrow { ReviewNotFoundException() }
        review.hide()
    }

    @Transactional(readOnly = true)
    fun list(
        productId: Long,
        sort: ReviewSort,
        page: Int,
        size: Int,
    ): Page<ReviewEntity> {
        val order =
            when (sort) {
                ReviewSort.LATEST -> Sort.by(Sort.Direction.DESC, "id")
                ReviewSort.RATING -> Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.DESC, "id"))
            }
        return reviewRepository.findByProductIdAndHiddenFalse(productId, PageRequest.of(page, size, order))
    }

    @Transactional(readOnly = true)
    fun summary(productId: Long): ReviewSummary =
        ReviewSummary(
            average = reviewRepository.averageRating(productId),
            count = reviewRepository.countByProductIdAndHiddenFalse(productId),
        )
}
