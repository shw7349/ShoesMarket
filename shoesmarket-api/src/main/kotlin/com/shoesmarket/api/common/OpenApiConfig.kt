package com.shoesmarket.api.common

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun shoesMarketOpenAPI(): OpenAPI =
        OpenAPI().info(
            Info()
                .title("ShoesMarket API")
                .description("선착순 한정판 스니커즈 드롭 시스템 API")
                .version("v1"),
        )
}
