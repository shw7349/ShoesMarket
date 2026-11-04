package com.shoesmarket.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.shoesmarket"])
class ShoesMarketApiApplication

fun main(args: Array<String>) {
    runApplication<ShoesMarketApiApplication>(*args)
}
