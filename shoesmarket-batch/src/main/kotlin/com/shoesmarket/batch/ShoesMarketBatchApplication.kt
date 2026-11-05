package com.shoesmarket.batch

import com.shoesmarket.payment.TossProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.Clock

@SpringBootApplication(scanBasePackages = ["com.shoesmarket"])
@EnableScheduling
@EnableJpaAuditing
@EnableConfigurationProperties(TossProperties::class)
@EntityScan(basePackages = ["com.shoesmarket.infra.persistence"])
@EnableJpaRepositories(basePackages = ["com.shoesmarket.infra.persistence"])
class ShoesMarketBatchApplication {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}

fun main(args: Array<String>) {
    runApplication<ShoesMarketBatchApplication>(*args)
}
