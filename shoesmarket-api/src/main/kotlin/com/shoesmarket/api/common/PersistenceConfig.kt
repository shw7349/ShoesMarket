package com.shoesmarket.api.common

import com.shoesmarket.api.incident.NotionProperties
import com.shoesmarket.payment.TossProperties
import com.shoesmarket.queue.QueueProperties
import com.shoesmarket.stock.ReservationProperties
import com.shoesmarket.stock.StockProperties
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * JPA/스케줄링/설정 바인딩 활성화를 메인 애플리케이션 클래스에서 분리한다.
 * 이렇게 하면 @WebMvcTest 같은 슬라이스 테스트가 JPA 인프라를 끌어오지 않는다.
 */
@Configuration
@EnableJpaAuditing
@EnableScheduling
@EnableConfigurationProperties(
    StockProperties::class,
    ReservationProperties::class,
    NotionProperties::class,
    QueueProperties::class,
    TossProperties::class,
)
@EntityScan(basePackages = ["com.shoesmarket.infra.persistence"])
@EnableJpaRepositories(basePackages = ["com.shoesmarket.infra.persistence"])
class PersistenceConfig
