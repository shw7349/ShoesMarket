rootProject.name = "ShoesMarket"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

// Phase 1: 순수 Kotlin 도메인 모듈부터 TDD로 시작.
// 이후 Phase에서 infra / queue / stock / payment / commerce / api / batch 모듈을 추가한다.
include("shoesmarket-domain")
