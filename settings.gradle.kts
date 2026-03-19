pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

// Opzionale: risolve automaticamente le versioni degli strumenti Java
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "prova-alchemist"
