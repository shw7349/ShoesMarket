package com.shoesmarket.stock

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "shoesmarket.stock")
class StockProperties {
    /** 재고 선점 전략: LUA | PESSIMISTIC | OPTIMISTIC | REDISSON */
    var strategy: String = "LUA"

    /** 선점 시 입장 토큰 검증 여부. Phase 2에서는 false로 두고 토큰 검증을 우회할 수 있다. */
    var tokenCheckEnabled: Boolean = true
}

@ConfigurationProperties(prefix = "shoesmarket.reservation")
class ReservationProperties {
    /** 선점 TTL(초). 선점 후 이 시간 내 결제하지 않으면 만료된다. */
    var ttlSeconds: Long = 300
}
