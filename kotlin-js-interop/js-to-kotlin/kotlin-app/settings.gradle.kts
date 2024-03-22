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
                from(files("../../../gradle/libs.versions.toml"))
            }
        }
    }
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "j2k-kotlin-app"
