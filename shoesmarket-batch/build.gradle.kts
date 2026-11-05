plugins {
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

// Spring Boot BOM이 kotlin-* 를 구버전으로 고정하지 않도록 우리 Kotlin 버전으로 덮어쓴다.
extra["kotlin.version"] = libs.versions.kotlin.get()

dependencies {
    implementation(project(":shoesmarket-domain"))
    implementation(project(":shoesmarket-infra"))
    implementation(project(":shoesmarket-payment"))
    // 재고 정합성 검증/복구에 Redis 키·반환이 필요해 stock에도 의존한다(명세 2.2 대비 추가).
    implementation(project(":shoesmarket-stock"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation(libs.kotlin.reflect)
    runtimeOnly(libs.mysql.connector)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testRuntimeOnly("com.h2database:h2")
}
