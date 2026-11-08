package com.shoesmarket.commerce

import com.shoesmarket.infra.persistence.OrderRepository
import com.shoesmarket.infra.persistence.ReviewEntity
import com.shoesmarket.infra.persistence.ReviewRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional

class ReviewServiceTest :
    DescribeSpec({
        val reviewRepository = mockk<ReviewRepository>(relaxed = true)
        val orderRepository = mockk<OrderRepository>()
        val service = ReviewService(reviewRepository, orderRepository)

        beforeEach {
            clearMocks(reviewRepository, orderRepository)
            every { reviewRepository.save(any()) } answers { firstArg() }
        }

        describe("후기 작성") {
            it("구매하지 않은 상품에는 작성할 수 없다") {
                every { orderRepository.hasPurchasedProduct(42, 1) } returns false

                shouldThrow<NotPurchasedException> {
                    service.write(productId = 1, memberId = 42, authorName = "철수", rating = 5, content = "좋아요", imageUrl = null)
                }
                verify(exactly = 0) { reviewRepository.save(any()) }
            }

            it("구매자는 후기를 작성할 수 있다") {
                every { orderRepository.hasPurchasedProduct(42, 1) } returns true

                val review = service.write(1, 42, "철수", 5, "좋아요", null)
                review.rating shouldBe 5
            }

            it("평점이 범위를 벗어나면 예외") {
                every { orderRepository.hasPurchasedProduct(42, 1) } returns true
                shouldThrow<IllegalArgumentException> { service.write(1, 42, "철수", 6, "x", null) }
            }
        }

        describe("후기 수정/삭제 권한") {
            it("작성자가 아니면 수정할 수 없다") {
                every { reviewRepository.findById(10) } returns Optional.of(ReviewEntity(1, 42, "철수", 4, "내용"))

                shouldThrow<ReviewForbiddenException> { service.update(10, memberId = 99, rating = 3, content = "수정", imageUrl = null) }
            }

            it("작성자가 아니면 삭제할 수 없다") {
                every { reviewRepository.findById(10) } returns Optional.of(ReviewEntity(1, 42, "철수", 4, "내용"))

                shouldThrow<ReviewForbiddenException> { service.delete(10, memberId = 99) }
            }
        }
    })
