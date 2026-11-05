package com.shoesmarket.api.drop

import com.shoesmarket.domain.DropStatus
import com.shoesmarket.infra.persistence.DropEventEntity
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class CreateDropRequest(
    @field:NotNull(message = "상품 ID는 필수입니다.")
    val productId: Long,
    @field:Min(value = 1, message = "총 재고는 1 이상이어야 합니다.")
    val totalStock: Int,
    @field:Min(value = 1, message = "1인 구매 제한은 1 이상이어야 합니다.")
    val perUserLimit: Int = 1,
    @field:NotNull(message = "오픈 시각은 필수입니다.")
    val openAt: Instant,
    @field:NotNull(message = "종료 시각은 필수입니다.")
    val closeAt: Instant,
)

data class DropResponse(
    val id: Long,
    val productId: Long,
    val totalStock: Int,
    val perUserLimit: Int,
    val openAt: Instant,
    val closeAt: Instant,
    val status: DropStatus,
) {
    companion object {
        fun from(entity: DropEventEntity): DropResponse =
            DropResponse(
                id = entity.id,
                productId = entity.productId,
                totalStock = entity.totalStock,
                perUserLimit = entity.perUserLimit,
                openAt = entity.openAt,
                closeAt = entity.closeAt,
                status = entity.status,
            )
    }
}

/**
 * 드롭 상세 (SPEC 6.2: 상태, 오픈 시각, 남은 재고 근사치).
 * 남은 재고(remainingStock)는 Phase 2에서 Redis 연동 후 실제 값으로 채운다.
 */
data class DropDetailResponse(
    val id: Long,
    val productId: Long,
    val status: DropStatus,
    val openAt: Instant,
    val closeAt: Instant,
    val totalStock: Int,
    val remainingStock: Int?,
) {
    companion object {
        fun from(
            entity: DropEventEntity,
            remainingStock: Int?,
        ): DropDetailResponse =
            DropDetailResponse(
                id = entity.id,
                productId = entity.productId,
                status = entity.status,
                openAt = entity.openAt,
                closeAt = entity.closeAt,
                totalStock = entity.totalStock,
                remainingStock = remainingStock,
            )
    }
}
