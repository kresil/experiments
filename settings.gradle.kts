pluginManagement {
    includeBuild("convention-plugins")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kresil-experiments"
include(":kmp")
include(":ktor")
include(":kotlin-js-interop")
include(":android-app")
