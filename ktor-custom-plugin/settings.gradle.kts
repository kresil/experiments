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

rootProject.name = "ktor-custom-plugin"

include(":end-to-end-utilities")
include(":client-custom-plugin")
include(":server-custom-plugin")
project(":end-to-end-utilities").projectDir = file("../ktor-retry-plugin/end-to-end-utilities")
