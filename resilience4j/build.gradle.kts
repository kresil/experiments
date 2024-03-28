plugins {
    alias(libs.plugins.kotlinJvm)
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()

}

dependencies {
    implementation(libs.resilience4j.retry)
    testImplementation(libs.mockito.core)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.junit.jupiter.engine)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}