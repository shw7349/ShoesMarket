package com.shoesmarket.payment

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "shoesmarket.payment.toss")
class TossProperties {
    var secretKey: String = ""
    var baseUrl: String = "https://api.tosspayments.com"
}
