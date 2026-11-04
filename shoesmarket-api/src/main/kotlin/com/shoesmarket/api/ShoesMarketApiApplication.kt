package com.shoesmarket.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.shoesmarket"])
@EnableJpaAuditing
@EntityScan(basePackages = ["com.shoesmarket.infra.persistence"])
@EnableJpaRepositories(basePackages = ["com.shoesmarket.infra.persistence"])
class ShoesMarketApiApplication

fun main(args: Array<String>) {
    runApplication<ShoesMarketApiApplication>(*args)
}
