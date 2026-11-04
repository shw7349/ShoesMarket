package com.shoesmarket.api.drop

import com.shoesmarket.api.common.ApiException
import com.shoesmarket.api.common.ErrorCode
import com.shoesmarket.infra.persistence.DropEventEntity
import com.shoesmarket.infra.persistence.DropEventRepository
import com.shoesmarket.infra.persistence.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DropService(
    private val dropEventRepository: DropEventRepository,
    private val productRepository: ProductRepository,
) {
    @Transactional
    fun register(request: CreateDropRequest): DropResponse {
        require(request.openAt.isBefore(request.closeAt)) { "오픈 시각은 종료 시각보다 앞서야 합니다." }
        if (!productRepository.existsById(request.productId)) {
            throw ApiException(ErrorCode.PRODUCT_NOT_FOUND)
        }
        val entity =
            DropEventEntity(
                productId = request.productId,
                totalStock = request.totalStock,
                perUserLimit = request.perUserLimit,
                openAt = request.openAt,
                closeAt = request.closeAt,
            )
        return DropResponse.from(dropEventRepository.save(entity))
    }

    @Transactional(readOnly = true)
    fun getDetail(dropId: Long): DropDetailResponse {
        val entity =
            dropEventRepository.findById(dropId).orElseThrow { ApiException(ErrorCode.DROP_NOT_FOUND) }
        // 남은 재고는 Phase 2에서 Redis 연동 후 채운다. 지금은 null.
        return DropDetailResponse.from(entity, remainingStock = null)
    }
}
