pluginManagement {
    repositories {
        maven {
            name = "GTNH Maven"
            url = uri("https://nexus.gtnewhorizons.com/repository/public/")
            mavenContent {
                includeGroup("com.gtnewhorizons")
                includeGroup("com.gtnewhorizons.retrofuturagradle")
            }
        }
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlinVersion", settings.extra.properties["kotlin_version"].toString())
        }
    }
}

rootProject.name = "SophisticatedBackpacks-GTNH"
