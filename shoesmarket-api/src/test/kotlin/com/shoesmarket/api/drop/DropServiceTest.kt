package com.shoesmarket.api.drop

import com.shoesmarket.api.common.ApiException
import com.shoesmarket.api.common.ErrorCode
import com.shoesmarket.domain.DropStatus
import com.shoesmarket.infra.persistence.DropEventEntity
import com.shoesmarket.infra.persistence.DropEventRepository
import com.shoesmarket.infra.persistence.ProductRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import java.util.Optional

class DropServiceTest :
    DescribeSpec({
        val dropEventRepository = mockk<DropEventRepository>()
        val productRepository = mockk<ProductRepository>()
        val dropService = DropService(dropEventRepository, productRepository)

        val openAt = Instant.parse("2026-11-20T11:00:00Z")
        val closeAt = Instant.parse("2026-11-20T12:00:00Z")

        fun request(
            productId: Long = 1L,
            open: Instant = openAt,
            close: Instant = closeAt,
        ) = CreateDropRequest(productId = productId, totalStock = 500, perUserLimit = 1, openAt = open, closeAt = close)

        describe("드롭 등록") {
            it("상품이 존재하면 SCHEDULED 상태로 저장한다") {
                every { productRepository.existsById(1L) } returns true
                every { dropEventRepository.save(any()) } answers { firstArg() }

                val response = dropService.register(request())

                response.status shouldBe DropStatus.SCHEDULED
                response.totalStock shouldBe 500
            }

            it("상품이 없으면 PRODUCT_NOT_FOUND") {
                every { productRepository.existsById(1L) } returns false

                val ex = shouldThrow<ApiException> { dropService.register(request()) }
                ex.errorCode shouldBe ErrorCode.PRODUCT_NOT_FOUND
            }

            it("오픈 시각이 종료 시각보다 늦으면 예외") {
                shouldThrow<IllegalArgumentException> {
                    dropService.register(request(open = closeAt, close = openAt))
                }
            }
        }

        describe("드롭 상세 조회") {
            it("존재하지 않으면 DROP_NOT_FOUND") {
                every { dropEventRepository.findById(999L) } returns Optional.empty()

                val ex = shouldThrow<ApiException> { dropService.getDetail(999L) }
                ex.errorCode shouldBe ErrorCode.DROP_NOT_FOUND
            }

            it("존재하면 상세를 반환한다(남은 재고는 아직 null)") {
                every { dropEventRepository.findById(1L) } returns
                    Optional.of(DropEventEntity(1L, 500, 1, openAt, closeAt))

                val response = dropService.getDetail(1L)
                response.totalStock shouldBe 500
                response.remainingStock shouldBe null
            }
        }
    })
