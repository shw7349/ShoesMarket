package com.shoesmarket.api.media

import com.shoesmarket.api.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Upload", description = "이미지 업로드 API")
@RestController
class UploadController(
    private val imageStorage: ImageStorage,
) {
    @Operation(summary = "이미지 업로드")
    @PostMapping("/api/v1/uploads")
    fun upload(
        @RequestParam("file") file: MultipartFile,
    ): ApiResponse<Map<String, String>> = ApiResponse.success(mapOf("url" to imageStorage.store(file)))
}
