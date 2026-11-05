dependencies {
    implementation(rootProject.libs.kotlin.reflect)

    testImplementation(rootProject.libs.kotest.runner.junit5)
    testImplementation(rootProject.libs.kotest.assertions.core)
    testImplementation(rootProject.libs.kotest.property)
}
