package com.shoesmarket.api.media

import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

/**
 * 이미지 저장 추상화. 지금은 로컬 디스크에 저장하고, 필요하면 S3 구현으로 교체한다.
 */
interface ImageStorage {
    /** 저장 후 접근 가능한 URL 경로를 반환한다. */
    fun store(file: MultipartFile): String
}

@Component
class LocalImageStorage(
    private val properties: MediaProperties,
) : ImageStorage {
    override fun store(file: MultipartFile): String {
        require(!file.isEmpty) { "빈 파일입니다." }
        val ext =
            file.originalFilename
                ?.substringAfterLast('.', "")
                ?.lowercase()
                .orEmpty()
        val name = UUID.randomUUID().toString() + if (ext.isNotBlank()) ".$ext" else ""
        val dir = Paths.get(properties.dir).toAbsolutePath()
        Files.createDirectories(dir)
        file.inputStream.use { input ->
            Files.copy(input, dir.resolve(name))
        }
        return "/media/$name"
    }
}
