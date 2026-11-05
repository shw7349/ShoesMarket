plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktlint) apply false
}

group = "com.shoesmarket"
version = "0.0.1-SNAPSHOT"

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    repositories {
        mavenCentral()
    }

    // JDK 21 toolchain (로컬 설치된 JDK 21 사용)
    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(21)
    }

    // ktlint 도구 버전을 Kotlin 2.2 호환(1.6.0)으로 고정
    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set(rootProject.libs.versions.ktlintTool.get())
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
