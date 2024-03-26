pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    dependencyResolutionManagement {
        versionCatalogs {
            create("libs") {
                from(files("../gradle/libs.versions.toml"))
            }
        }
    }
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ktor"

include(":shared")
include(":android-app")