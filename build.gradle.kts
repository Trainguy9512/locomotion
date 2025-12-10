@file:Suppress("UnstableApiUsage")

plugins {
	id("dev.architectury.loom")
	id("architectury-plugin")
}

val minecraft = stonecutter.current.version

version = "${prop("mod.version")}+$minecraft-playtesting"
base {
	archivesName.set("${prop("mod.id")}-common")
}

architectury.common(stonecutter.tree.branches.mapNotNull {
	if (stonecutter.current.project !in it) null
	else it.project.prop("loom.platform")
})

loom {
	silentMojangMappingsLicense()
	accessWidenerPath = rootProject.file("src/main/resources/${prop("mod.id")}.accesswidener")

	decompilers {
		get("vineflower").apply { // Adds names to lambdas - useful for mixins
			options.put("mark-corresponding-synthetics", "1")
		}
	}
}

repositories {
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
//		mappings("dev.lambdaurora:${versionProp("yalmm")}")
	})
	modImplementation("net.fabricmc:fabric-loader:${versionProp("fabric_loader")}")

	// Mod implementations
	modCompileOnly("dev.isxander:yet-another-config-lib:${versionProp("yacl_version")}-fabric")
}

tasks.processResources {
	applyProperties(project, listOf("${prop("mod.id")}-common.mixin.json"))
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