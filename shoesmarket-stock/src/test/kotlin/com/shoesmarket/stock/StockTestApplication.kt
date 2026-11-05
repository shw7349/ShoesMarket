package com.shoesmarket.stock

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import java.time.Clock

@SpringBootApplication(scanBasePackages = ["com.shoesmarket"])
@EnableJpaAuditing
@EntityScan(basePackages = ["com.shoesmarket.infra.persistence"])
@EnableJpaRepositories(basePackages = ["com.shoesmarket.infra.persistence"])
@EnableConfigurationProperties(StockProperties::class, ReservationProperties::class)
class StockTestApplication {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
