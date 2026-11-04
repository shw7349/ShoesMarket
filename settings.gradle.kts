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

// Phase 1: domain → infra → api 순으로 구성.
// 이후 Phase에서 queue / stock / payment / commerce / batch 모듈을 추가한다.
include("shoesmarket-domain")
include("shoesmarket-infra")
include("shoesmarket-stock")
include("shoesmarket-api")
