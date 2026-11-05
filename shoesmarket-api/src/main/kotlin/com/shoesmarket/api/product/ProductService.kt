package com.shoesmarket.api.product

import com.shoesmarket.api.common.ApiException
import com.shoesmarket.api.common.ErrorCode
import com.shoesmarket.domain.Money
import com.shoesmarket.infra.persistence.ProductEntity
import com.shoesmarket.infra.persistence.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
) {
    @Transactional
    fun register(request: CreateProductRequest): ProductResponse {
        // Money로 금액 불변식(>=0) 보장
        val price = Money(request.price)
        val entity = ProductEntity(name = request.name, brand = request.brand, price = price.amount)
        return ProductResponse.from(productRepository.save(entity))
    }

    @Transactional(readOnly = true)
    fun get(productId: Long): ProductResponse {
        val entity =
            productRepository.findById(productId).orElseThrow { ApiException(ErrorCode.PRODUCT_NOT_FOUND) }
        return ProductResponse.from(entity)
    }
}
