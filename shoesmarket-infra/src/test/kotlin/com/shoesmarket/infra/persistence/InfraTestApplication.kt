package com.shoesmarket.infra.persistence

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/**
 * infra 모듈 슬라이스 테스트(@DataJpaTest)가 부트스트랩할 설정.
 * 라이브러리 모듈이라 실행용 @SpringBootApplication이 없으므로 테스트 전용으로 둔다.
 */
@SpringBootApplication
@EnableJpaAuditing
class InfraTestApplication
