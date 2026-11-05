package com.shoesmarket.queue

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "shoesmarket.queue")
class QueueProperties {
    /** 초당 입장 인원 */
    var admitPerSecond: Int = 100

    /** 입장 토큰 TTL(초) */
    var tokenTtlSeconds: Long = 300
}
