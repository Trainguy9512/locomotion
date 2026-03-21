@file:Suppress("UnstableApiUsage")

import java.util.jar.JarFile

plugins {
    id("dev.architectury.loom")
    id("com.github.johnrengelman.shadow")
    id("architectury-plugin")
}

val loader = prop("loom.platform")!!
val minecraft: String = stonecutter.current.version
val common: Project = requireNotNull(stonecutter.node.sibling("")) {
    "No common project for $project"
}.project

version = "${prop("mod.version")}+$minecraft-playtesting"
base {
    archivesName.set("${prop("mod.id")}-$loader")
}

architectury {
    platformSetupLoomIde()
    fabric()
    compileOnly()
}

loom {
    silentMojangMappingsLicense()

    decompilers {
        get("vineflower").apply { // Adds names to lambdas - useful for mixins
            options.put("mark-corresponding-synthetics", "1")
        }
    }

    runs {
        val runDir = "../../../.runs"

        named("client") {
            client()
            configName = "Client"
            runDir("$runDir/client")
            source(sourceSets["main"])
            programArgs("--username=Dev")
            val quickPlayWorld = System.getenv("LOCOMOTION_QUICKPLAY_WORLD")
            if (!quickPlayWorld.isNullOrBlank()) {
                programArgs("--quickPlaySingleplayer", quickPlayWorld)
            }
        }
        named("server") {
            server()
            configName = "Server"
            runDir("$runDir/server")
            source(sourceSets["main"])
        }
    }

    sourceSets {
        main {
            resources {
                srcDir(project.file("versions/$minecraft/src/main/generated"))
            }
        }
    }

    runConfigs.all {
        isIdeConfigGenerated = true
    }
}

val commonBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val shadowBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

configurations {
    compileClasspath.get().extendsFrom(commonBundle)
    runtimeClasspath.get().extendsFrom(commonBundle)
    get("developmentFabric").extendsFrom(commonBundle)
}

configurations.named("runtimeClasspath") {
    withDependencies {
        removeIf { it.group == "dev.architectury" && it.name == "architectury-transformer" }
    }
}

configurations.matching { it.name == "architecturyTransformerRuntimeClasspath" }.all {
    withDependencies {
        removeIf { it.group == "dev.architectury" && it.name == "architectury-transformer" }
    }
}

repositories {
    maven { url = uri("${rootDir}/.local-maven") }
    maven("https://maven.parchmentmc.org/")

    maven("https://maven.terraformersmc.com/")
    maven("https://maven.isxander.dev/releases")
    maven("https://api.modrinth.com/maven")
    maven {
        name = "Gegy"
        url = uri("https://maven.gegy.dev/releases/")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${versionProp("parchment_minecraft_version")}:${versionProp("parchment_mappings_version")}@zip")
//        mappings("dev.lambdaurora:${versionProp("yalmm")}")
    })
    modImplementation("net.fabricmc:fabric-loader:${versionProp("fabric_loader")}")

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "namedElements")) { isTransitive = false }

    // Mod implementations
    modImplementation("net.fabricmc.fabric-api:fabric-api:${versionProp("fabric_api_version")}")
    modImplementation("dev.isxander:yet-another-config-lib:${versionProp("yacl_version")}-fabric")
    modImplementation("com.terraformersmc:modmenu:${versionProp("modmenu_version")}")
    modImplementation("maven.modrinth:sodium:${versionProp("sodium_version")}")
//    modImplementation("maven.modrinth:iris:${versionProp("iris_version")}")

    // Iris dependencies
    runtimeOnly("org.antlr:antlr4-runtime:4.13.1")
    runtimeOnly("io.github.douira:glsl-transformer:2.0.1")
    runtimeOnly("org.anarres:jcpp:1.4.14")

    runtimeOnly(files("${rootDir}/.local-maven/dev/architectury/architectury-transformer/5.2.87/architectury-transformer-5.2.87.jar"))
    runtimeOnly(files("${rootDir}/.local-maven/dev/architectury/architectury-transformer/5.2.87/architectury-transformer-5.2.87-runtime.jar"))

    add("architecturyTransformerRuntimeClasspath", files("${rootDir}/.local-maven/dev/architectury/architectury-transformer/5.2.87/architectury-transformer-5.2.87.jar"))
    add("architecturyTransformerRuntimeClasspath", files("${rootDir}/.local-maven/dev/architectury/architectury-transformer/5.2.87/architectury-transformer-5.2.87-runtime.jar"))

}

tasks.processResources {
    applyProperties(project, listOf("fabric.mod.json", "${prop("mod.id")}-fabric.mixin.json"))
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier = "dev-shadow"
}

tasks.remapJar {
    injectAccessWidener = true
    input = tasks.shadowJar.get().archiveFile
    archiveClassifier = null
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    archiveClassifier = "dev"
}

java {
    withSourcesJar()
    val java = if (stonecutter.eval(minecraft, ">=1.20.5"))
        JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    targetCompatibility = java
    sourceCompatibility = java
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
}

tasks.register<Copy>("buildAndCollect") {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
    from(tasks.remapJar.get().archiveFile, tasks.remapSourcesJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${prop("mod.version")}/$loader"))
    dependsOn("build")
}

tasks.register("verifyFabricJar") {
    group = "verification"
    description = "Verifies the remapped Fabric jar contains required common assets and mixin metadata."
    dependsOn("setupChiseledBuild")
    dependsOn(tasks.remapJar)
    doLast {
        val jarFile = tasks.remapJar.get().archiveFile.get().asFile
        val requiredEntries = listOf(
            "fabric.mod.json",
            "${prop("mod.id")}-common.mixins.json",
            "${prop("mod.id")}-common.refmap.json",
            "com/trainguy9512/locomotion/LocomotionMain.class"
        )
        JarFile(jarFile).use { jar ->
            val missing = requiredEntries.filter { jar.getEntry(it) == null }
            if (missing.isNotEmpty()) {
                error("Missing required entries in ${jarFile.name}: ${missing.joinToString(", ")}")
            }
        }
    }
}

fabricApi {
    configureDataGeneration {
        client = true
        modId = "${prop("mod.id")}"
    }
}

stonecutter {
    // Constants should be given a key and a boolean value
    const("fabric", loader == "fabric")
    const("forge", loader == "forge")
    const("neoforge", loader == "neoforge")
}
