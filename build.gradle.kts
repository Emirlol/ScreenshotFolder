plugins {
	alias(libs.plugins.loom)
	alias(libs.plugins.kotlin)
	alias(libs.plugins.modPublish)
}

repositories {
	mavenCentral()
	// Yacl is now also on mavencentral but its quilt dependencies are not, which is just weird
	exclusiveContent {
		forRepository {
			maven("https://maven.isxander.dev/releases") { name = "Xander Maven" }
		}
		filter {
			includeGroupAndSubgroups("org.quiltmc")
		}
	}
	exclusiveContent {
		forRepository {
			maven("https://maven.terraformersmc.com/releases") { name = "Terraformers" }
		}
		filter {
			includeGroupAndSubgroups("com.terraformersmc")
		}
	}
	exclusiveContent {
		forRepositories(
			maven("https://ancientri.me/maven/snapshots") {
				name = "AncientRimeSnapshots"
				mavenContent { snapshotsOnly() }
			},
			maven("https://ancientri.me/maven/releases") {
				name = "AncientRimeReleases"
				mavenContent { releasesOnly() }
			}
		)
		filter {
			includeGroupAndSubgroups("me.ancientri")
		}
	}
}

val modName = property("mod_name") as String
val modId = property("mod_id") as String
group = property("maven_group") as String
val modVersion = property("mod_version") as String
version = "$modVersion+${libs.versions.minecraft.get()}"

dependencies {
	minecraft(libs.minecraft)
	mappings(variantOf(libs.yarnMappings) { classifier("v2") })
	modImplementation(libs.fabricLoader)

	modImplementation(libs.fabricApi)
	modImplementation(libs.fabricLanguageKotlin)
	modImplementation(libs.yacl)
	modImplementation(libs.modMenu)
	modImplementation(libs.rimelib)
}

tasks {
	processResources {
		val props = mapOf(
			"version" to version,
			"minecraft_version" to libs.versions.minecraft.get(),
			"loader_version" to libs.versions.fabricLoader.get(),
			"fabric_kotlin_version" to libs.versions.fabricLanguageKotlin.get(),
			"modmenu_version" to libs.versions.modMenu.get(),
			"yacl_version" to libs.versions.yacl.get(),
			"rimelib_version" to libs.versions.rimelib.get()
		)
		inputs.properties(props)
		filesMatching("fabric.mod.json") {
			expand(props)
		}
	}
	jar {
		from("LICENSE") {
			rename { "${it}_${base.archivesName.get()}" }
		}
	}
}

kotlin {
	jvmToolchain(21)
}

publishMods {
	file = tasks.remapJar.get().archiveFile
	modLoaders.add("fabric")
	type = STABLE
	displayName = "Screenshot Folder ${project.version}"
	changelog = ""

	modrinth {
		accessToken = providers.environmentVariable("MODRINTH_TOKEN")
		projectId = "IapFyiRd"
		minecraftVersions.addAll(libs.versions.minecraft.get())
		requires("fabric-language-kotlin")
		requires("fabric-api")
		requires("yacl")
		requires("rimelib")
		optional("modmenu")
		projectDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText
		featured = true
	}
}