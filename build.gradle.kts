import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("root.publication")
    // trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}