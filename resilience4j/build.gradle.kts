plugins {
    alias(libs.plugins.kotlinJvm)
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // implementation(libs.resilience4j.retry) TODO: doesnt work with version catalog
    implementation("io.github.resilience4j:resilience4j-retry:2.2.0")
    testImplementation("org.mockito:mockito-core:5.3.1")
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    // Use the JUnit 5 integration.
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}