plugins {
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.dependency.management)
}

// Spring Boot BOM이 kotlin-* 를 구버전(1.9.25)으로 고정하지 않도록 우리 Kotlin 버전으로 덮어쓴다.
extra["kotlin.version"] = libs.versions.kotlin.get()

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.springBoot.get()}")
    }
}

dependencies {
    api(project(":shoesmarket-domain"))

    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-data-redis")
    implementation(libs.kotlin.reflect)
    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.mysql)
    runtimeOnly(libs.mysql.connector)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testRuntimeOnly("com.h2database:h2")
}

// JPA 엔티티를 지연 로딩 프록시가 상속할 수 있도록 open 처리
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
