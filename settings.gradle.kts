pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.neoforged.net/releases/")
//		maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    // Make sure the version here is the same as the dependency in buildSrc/build.gradle.kts.kts
    id("dev.kikugie.stonecutter") version "0.5.1"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    create(rootProject) {
        versions("1.21.10")
        vcsVersion = "1.21.10"
        branch("fabric")
        //branch("forge") { versions("1.21.5") }+
        branch("neoforge") { versions("1.21.10") }
    }
}

rootProject.name = "Locomotion"