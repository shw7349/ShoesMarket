plugins {
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

// Spring Boot BOM이 kotlin-* 를 구버전(1.9.25)으로 고정하지 않도록 우리 Kotlin 버전으로 덮어쓴다.
extra["kotlin.version"] = libs.versions.kotlin.get()

dependencies {
    implementation(project(":shoesmarket-domain"))
    implementation(project(":shoesmarket-infra"))
    implementation(project(":shoesmarket-stock"))
    implementation(project(":shoesmarket-queue"))
    implementation(project(":shoesmarket-payment"))
    implementation(project(":shoesmarket-commerce"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.kotlin.reflect)
    implementation(libs.springdoc.openapi)

    runtimeOnly(libs.mysql.connector)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testImplementation(libs.springmockk)
    testRuntimeOnly("com.h2database:h2")
}
