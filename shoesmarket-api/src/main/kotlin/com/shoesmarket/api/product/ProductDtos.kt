package com.shoesmarket.api.product

import com.shoesmarket.infra.persistence.ProductEntity
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.time.Instant

data class CreateProductRequest(
    @field:NotBlank(message = "상품명은 필수입니다.")
    val name: String,
    @field:NotBlank(message = "브랜드는 필수입니다.")
    val brand: String,
    @field:Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    val price: Long,
)

data class ProductResponse(
    val id: Long,
    val name: String,
    val brand: String,
    val price: Long,
    val createdAt: Instant,
) {
    companion object {
        fun from(entity: ProductEntity): ProductResponse =
            ProductResponse(
                id = entity.id,
                name = entity.name,
                brand = entity.brand,
                price = entity.price,
                createdAt = entity.createdAt,
            )
    }
}
