package com.shoesmarket.api.media

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@ConfigurationProperties(prefix = "shoesmarket.media")
class MediaProperties {
    /** 업로드 이미지를 저장할 로컬 디렉토리. S3 등으로 교체 가능하도록 ImageStorage 뒤에 둔다. */
    var dir: String = "media"
}

@Configuration
class MediaWebConfig(
    private val properties: MediaProperties,
) : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val location =
            Paths
                .get(properties.dir)
                .toAbsolutePath()
                .toUri()
                .toString()
        registry.addResourceHandler("/media/**").addResourceLocations(location)
    }
}
