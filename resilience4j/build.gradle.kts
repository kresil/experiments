plugins {
    alias(libs.plugins.kotlinJvm)
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    testImplementation(libs.resilience4j.circuitbreaker)
    testImplementation(libs.resilience4j.ratelimiter)
    testImplementation(libs.resilience4j.retry)
    testImplementation(libs.resilience4j.kotlin)
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.junit.jupiter.engine)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
