package com.shoesmarket.infra.persistence

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class ProductRepositoryTest
    @Autowired
    constructor(
        private val productRepository: ProductRepository,
        private val em: TestEntityManager,
    ) {
        @Test
        fun `상품을 저장하고 조회하면 동일한 값과 감사 시각이 채워진다`() {
            val saved = productRepository.save(ProductEntity(name = "에어포스1", brand = "나이키", price = 139_000))
            em.flush()
            em.clear()

            val found = productRepository.findById(saved.id).orElseThrow()

            assertThat(found.name).isEqualTo("에어포스1")
            assertThat(found.brand).isEqualTo("나이키")
            assertThat(found.price).isEqualTo(139_000)
            assertThat(found.createdAt).isNotEqualTo(java.time.Instant.EPOCH)
        }
    }
