@file:Suppress("UnstableApiUsage")

plugins {
    id("dev.kikugie.loom-back-compat")
    id("me.modmuss50.mod-publish-plugin")
    id("maven-publish")
}

val minecraft = stonecutter.current.version
val isLegacy = stonecutter.eval(minecraft, "<26.1")
val javaVersion = property("deps.java_version") as String
val modId = property("mod.id") as String

val overlays = mapOf(
    "1.21.1" to "<26.1",
    "26.x" to ">=26.1",
    "26.1" to ">=26.1 <26.2",
    "26.2+" to ">=26.2",
)
val activeOverlays = overlays.filter { stonecutter.eval(minecraft, it.value) }.keys
sourceSets.main {
    resources.srcDir(rootProject.file("src/generated/resources"))
    activeOverlays.forEach { resources.srcDir(rootProject.file("src/main/overlays/$it")) }
}

tasks.named<ProcessResources>("processResources") {
    fun prop(name: String) = project.property(name) as String

    val props = HashMap<String, String>().apply {
        this["version"] = prop("mod.version") + "+" + prop("deps.minecraft")
        this["minecraft"] = prop("mod.mc_dep_fabric")
        this["mod_id"] = prop("mod.id")
        this["mod_name"] = prop("mod.name")
        this["description"] = prop("mod.description")
        this["mod_author"] = prop("mod.author")
        this["credits"] = prop("mod.credits")
        this["license"] = prop("mod.license")
        this["fabric_loader_version"] = prop("deps.fabric_loader")
        this["java_version"] = prop("deps.java_version")
    }
    inputs.properties(props)

    filesMatching(listOf("fabric.mod.json", "*.mixins.json")) {
        expand(props)
    }
}

tasks.named("processResources") {
    dependsOn(":${stonecutter.current.project}:stonecutterGenerate")
}

tasks.named<ProcessResources>("processResources") {
    doLast {
        fun convert(node: Any?): Any? = when (node) {
            is Map<*, *> -> {
                val result = LinkedHashMap<String, Any?>()
                node.forEach { (key, value) ->
                    when {
                        key == "neoforge:conditions" -> result["fabric:load_conditions"] = (value as List<*>).map { condition ->
                            val map = condition as Map<*, *>
                            require(map["type"] == "neoforge:mod_loaded") { "Unsupported NeoForge condition ${map["type"]}" }
                            mapOf("condition" to "fabric:all_mods_loaded", "values" to listOf(map["modid"]))
                        }
                        else -> result[key as String] = convert(value)
                    }
                }
                result
            }
            is List<*> -> node.map { convert(it) }
            else -> node
        }

        val output = (this as ProcessResources).destinationDir.resolve("data")
        output.walk().filter { it.isFile && it.extension == "json" && it.readText().contains("\"neoforge:conditions\"") }.forEach { file ->
            val converted = convert(groovy.json.JsonSlurper().parseText(file.readText()))
            file.writeText(groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(converted)))
        }
    }
}

version = "${property("mod.version")}+${property("deps.minecraft")}-fabric"
base.archivesName = modId

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "Terraformers (Mod Menu)"
        url = uri("https://maven.terraformersmc.com/releases/")
        content {
            includeGroupAndSubgroups("com.terraformersmc")
        }
    }
    maven {
        name = "Xander Maven (YACL)"
        url = uri("https://maven.isxander.dev/releases")
        content {
            includeGroupAndSubgroups("dev.isxander")
            includeGroupAndSubgroups("org.quiltmc.parsers")
        }
    }
    maven {
        name = "Quilt Maven"
        url = uri("https://maven.quiltmc.org/repository/release/")
        content {
            includeGroupAndSubgroups("org.quiltmc.parsers")
        }
    }
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroupAndSubgroups("maven.modrinth")
        }
    }
}

val yaclModrinthOnly = findProperty("deps.yacl_modrinth_only") == "true"
val yaclCoordinates = property("deps.yacl").toString().let {
    if (yaclModrinthOnly) "maven.modrinth:yacl:$it" else "dev.isxander:yet-another-config-lib:$it"
}
val yaclLibrariesMissingFromModrinth = listOf(
    "org.quiltmc.parsers:json:0.2.1",
    "org.quiltmc.parsers:gson:0.2.1",
    "com.twelvemonkeys.imageio:imageio-core:3.12.0",
    "com.twelvemonkeys.imageio:imageio-webp:3.12.0",
    "com.twelvemonkeys.imageio:imageio-metadata:3.12.0",
    "com.twelvemonkeys.common:common-lang:3.12.0",
    "com.twelvemonkeys.common:common-io:3.12.0",
    "com.twelvemonkeys.common:common-image:3.12.0",
)

dependencies {
    minecraft("com.mojang:minecraft:${property("deps.minecraft")}")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    // YACL
    modImplementation(yaclCoordinates)
    if (yaclModrinthOnly) yaclLibrariesMissingFromModrinth.forEach { runtimeOnly(it) }

    // Mod Menu
    modImplementation("com.terraformersmc:modmenu:${property("deps.modmenu")}")

    // Raised
    modCompileOnly("maven.modrinth:raised:${property("deps.raised")}")

    // Reliable Remover
    modCompileOnly("maven.modrinth:reliable-remover:${property("deps.reliable_remover")}")

    // Reliable Gliders
    modCompileOnly("maven.modrinth:reliable-gliders:${property("deps.reliable_gliders")}")
    modRuntimeOnly("maven.modrinth:reliable-gliders:${property("deps.reliable_gliders")}")

    // Combat Roll
    modCompileOnly("maven.modrinth:combat-roll:${property("deps.combat_roll")}")

    // Farmer's Delight Refabricated
    modCompileOnly("maven.modrinth:farmers-delight-refabricated:${property("deps.farmers_delight")}")
    modRuntimeOnly("maven.modrinth:farmers-delight-refabricated:${property("deps.farmers_delight")}")

    // Cultural Delights
    modCompileOnly("maven.modrinth:cultural-delights:${property("deps.cultural_delights")}")
    modRuntimeOnly("maven.modrinth:cultural-delights:${property("deps.cultural_delights")}")
    modCompileOnly("maven.modrinth:asphodel-api:${property("deps.asphodel")}")
    if (isLegacy) modRuntimeOnly("maven.modrinth:asphodel-api:${property("deps.asphodel")}")

    if (!isLegacy) {
        // RRV
        modCompileOnly("maven.modrinth:rrv:${property("deps.rrv")}")
        modRuntimeOnly("maven.modrinth:rrv:${property("deps.rrv")}")
    }

    if (isLegacy) {
        // EMI
        modCompileOnly("maven.modrinth:emi:${property("deps.emi")}+fabric")
        modRuntimeOnly("maven.modrinth:emi:${property("deps.emi")}+fabric")

        // Thermoo
        modCompileOnly("maven.modrinth:thermoo:${property("deps.thermoo")}")

        // Shield Expansion
        modCompileOnly("maven.modrinth:shield-expansion:${property("deps.shield_expansion")}")
    }
}

val accessWidener = activeOverlays.map { rootProject.file("src/main/overlays/$it/$modId.accesswidener") }.firstOrNull { it.exists() }
if (accessWidener != null) loom {
    accessWidenerPath = accessWidener
}

loom {
    runs.configureEach {
        ideConfigGenerated(true)
        runDir = "run/$name"
    }
}

tasks {
    processResources {
        exclude("**/neoforge.mods.toml", "**/mods.toml", "**/accesstransformer.cfg", "data/neoforge/**", ".cache/**")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(loomx.modJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = javaVersion.toInt()
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "5000"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = property("mod.group") as String
            artifactId = "$modId-fabric"
            version = "${property("mod.version")}+${property("deps.minecraft")}"

            from(components["java"])
        }
    }
}

val additionalVersions: List<String> = (findProperty("publish.additionalVersions") as String?)
    ?.split(",")
    ?.map { it.trim() }
    ?.filter { it.isNotEmpty() }
    ?: emptyList()

publishMods {
    file = loomx.modJar.map { it.archiveFile.get() }
    additionalFiles.from(loomx.modSourcesJar.map { it.archiveFile.get() })

    type = STABLE
    displayName = "${property("mod.name")} Fabric ${stonecutter.current.project.substringBeforeLast('-')} - ${property("mod.version")}"
    version = "${property("mod.version")}+${property("deps.minecraft")}-fabric"
    changelog = provider { rootProject.file("CHANGELOG-LATEST.md").readText() }
    modLoaders.add("fabric")

    modrinth {
        projectId = property("publish.modrinth") as String
        accessToken = providers.environmentVariable("MODRINTH_TOKEN").orElse(providers.environmentVariable("MODRINTH_API_KEY"))
        minecraftVersions.add(property("deps.minecraft") as String)
        minecraftVersions.addAll(additionalVersions)
        requires("fabric-api", "yacl")
        optional("modmenu", "raised", "reliable-remover", "reliable-gliders", "combat-roll", "farmers-delight-refabricated", "cultural-delights")
        if (isLegacy) optional("emi", "thermoo", "shield-expansion") else optional("rrv")
        incompatible("melancholic_hunger")
    }

    curseforge {
        projectId = property("publish.curseforge") as String
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN").orElse(providers.environmentVariable("CURSEFORGE_API_KEY"))
        minecraftVersions.add(property("deps.minecraft") as String)
        minecraftVersions.addAll(additionalVersions)
        requires("fabric-api", "yacl")
        optional("modmenu", "raised", "reliable-remover", "reliable-gliders", "combat-roll", "farmers-delight-refabricated", "cultural-delights")
        if (isLegacy) optional("emi", "thermoo", "shield-expansion") else optional("rrv")
        client = true
        server = true
    }
}
