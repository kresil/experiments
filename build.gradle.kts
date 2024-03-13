plugins {
    id("root.publication")
    // trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    kotlin("jvm") version "1.9.23"
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
repositories {
    mavenCentral()
}
kotlin {
    jvmToolchain(8)
}