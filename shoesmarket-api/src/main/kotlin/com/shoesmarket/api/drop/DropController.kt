package com.shoesmarket.api.drop

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

@Tag(name = "Drop", description = "드롭 이벤트 API")
@RestController
class DropController(
    private val dropService: DropService,
) {
    @Operation(summary = "드롭 이벤트 등록")
    @PostMapping("/api/v1/admin/drops")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateDropRequest,
    ): ApiResponse<DropResponse> = ApiResponse.success(dropService.register(request))

    @Operation(summary = "드롭 상세 조회")
    @GetMapping("/api/v1/drops/{dropId}")
    fun getDetail(
        @PathVariable dropId: Long,
    ): ApiResponse<DropDetailResponse> = ApiResponse.success(dropService.getDetail(dropId))
}
