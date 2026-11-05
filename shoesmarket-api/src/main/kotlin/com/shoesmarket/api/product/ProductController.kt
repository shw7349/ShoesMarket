package com.shoesmarket.api.product

import com.shoesmarket.api.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Product", description = "상품 API")
@RestController
class ProductController(
    private val productService: ProductService,
) {
    @Operation(summary = "상품 등록")
    @PostMapping("/api/v1/admin/products")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateProductRequest,
    ): ApiResponse<ProductResponse> = ApiResponse.success(productService.register(request))

    @Operation(summary = "상품 조회")
    @GetMapping("/api/v1/products/{productId}")
    fun get(
        @PathVariable productId: Long,
    ): ApiResponse<ProductResponse> = ApiResponse.success(productService.get(productId))
}
