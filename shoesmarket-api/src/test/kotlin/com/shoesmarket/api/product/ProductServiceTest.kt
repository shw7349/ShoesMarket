package com.shoesmarket.api.product

import com.shoesmarket.api.common.ApiException
import com.shoesmarket.api.common.ErrorCode
import com.shoesmarket.infra.persistence.ProductEntity
import com.shoesmarket.infra.persistence.ProductRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.util.Optional

class ProductServiceTest :
    DescribeSpec({
        val productRepository = mockk<ProductRepository>()
        val productService = ProductService(productRepository)

        describe("상품 등록") {
            it("정상 요청이면 저장 후 응답을 반환한다") {
                every { productRepository.save(any()) } answers { firstArg() }

                val response = productService.register(CreateProductRequest(name = "에어포스1", brand = "나이키", price = 139_000))

                response.name shouldBe "에어포스1"
                response.brand shouldBe "나이키"
                response.price shouldBe 139_000
            }

            it("가격이 음수면 예외를 던진다") {
                shouldThrow<IllegalArgumentException> {
                    productService.register(CreateProductRequest(name = "x", brand = "y", price = -1))
                }
            }
        }

        describe("상품 조회") {
            it("존재하지 않으면 PRODUCT_NOT_FOUND") {
                every { productRepository.findById(999L) } returns Optional.empty()

                val ex = shouldThrow<ApiException> { productService.get(999L) }
                ex.errorCode shouldBe ErrorCode.PRODUCT_NOT_FOUND
            }

            it("존재하면 응답을 반환한다") {
                every { productRepository.findById(1L) } returns Optional.of(ProductEntity("에어맥스", "나이키", 189_000))

                val response = productService.get(1L)
                response.name shouldBe "에어맥스"
                response.price shouldBe 189_000
            }
        }
    })
