plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.nexus.publish)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
