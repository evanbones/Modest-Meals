plugins {
    id("net.neoforged.moddev")
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
        this["minecraft_version_range"] = prop("mod.mc_dep_forgelike")
        this["mod_id"] = prop("mod.id")
        this["mod_name"] = prop("mod.name")
        this["description"] = prop("mod.description")
        this["mod_author"] = prop("mod.author")
        this["credits"] = prop("mod.credits")
        this["license"] = prop("mod.license")
        this["neoforge_loader_version_range"] = prop("deps.neoforge_loader_version_range")
        this["neoforge_version"] = prop("deps.neoforge")
        this["logo_property"] = if (stonecutter.eval(minecraft, ">=26.2")) "iconFile" else "logoFile"
        this["yacl_version"] = prop("deps.yacl").substringBefore('+')
        this["java_version"] = prop("deps.java_version")
    }
    inputs.properties(props)

    filesMatching(listOf("META-INF/neoforge.mods.toml", "*.mixins.json")) {
        expand(props)
    }
}

version = "${property("mod.version")}+${property("deps.minecraft")}-neoforge"
base.archivesName = modId

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "Shedaniel (Cloth Config)"
        url = uri("https://maven.shedaniel.me/")
        content {
            includeGroupAndSubgroups("me.shedaniel")
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

neoForge {
    enable {
        version = property("deps.neoforge") as String
        isDisableRecompilation = true
    }
    validateAccessTransformers = true

    if (isLegacy) parchment {
        minecraftVersion = property("deps.parchment_minecraft") as String
        mappingsVersion = property("deps.parchment") as String
    }

    activeOverlays.map { rootProject.file("src/main/overlays/$it/META-INF/accesstransformer.cfg") }
        .firstOrNull { it.exists() }
        ?.let { accessTransformers.from(it.absolutePath) }

    runs {
        configureEach {
            systemProperty("neoforge.warnings.onlyin.hide", "true")
            systemProperty("neoforge.warnings.logofile.hide", "true")
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }
        register("client") {
            gameDirectory = file("run/client")
            client()
        }
        register("server") {
            gameDirectory = file("run/server")
            server()
        }
        if (isLegacy) register("data") {
            data()
            gameDirectory = file("run/data")
            programArguments.addAll(
                "--mod", modId, "--all",
                "--output", rootProject.file("src/generated/resources/").absolutePath,
                "--existing", rootProject.file("src/main/resources/").absolutePath
            )
        }
    }

    mods {
        register(modId) {
            sourceSet(sourceSets["main"])
        }
    }
}

// Blueprint's own data generators crash the data run, so these mods are copied into the client and server mods folders only
val gameplayMods: Configuration by configurations.creating {
    isTransitive = false
}

val yaclCoordinates = property("deps.yacl").toString().let {
    if (findProperty("deps.yacl_modrinth_only") == "true") "maven.modrinth:yacl:$it" else "dev.isxander:yet-another-config-lib:$it"
}

dependencies {
    // YACL
    implementation(yaclCoordinates)

    // Raised
    compileOnly("maven.modrinth:raised:${property("deps.raised")}")

    // Reliable Remover
    compileOnly("maven.modrinth:reliable-remover:${property("deps.reliable_remover")}")

    // Reliable Gliders
    compileOnly("maven.modrinth:reliable-gliders:${property("deps.reliable_gliders")}")
    runtimeOnly("maven.modrinth:reliable-gliders:${property("deps.reliable_gliders")}")

    // Combat Roll
    compileOnly("maven.modrinth:combat-roll:${property("deps.combat_roll")}")

    if (isLegacy) {
        // EMI
        compileOnly("maven.modrinth:emi:${property("deps.emi")}+neoforge")
        runtimeOnly("maven.modrinth:emi:${property("deps.emi")}+neoforge")

        // Farmer's Delight
        compileOnly("maven.modrinth:farmers-delight:${property("deps.farmers_delight")}")
        runtimeOnly("maven.modrinth:farmers-delight:${property("deps.farmers_delight")}")

        // Cultural Delights
        compileOnly("maven.modrinth:cultural-delights:${property("deps.cultural_delights")}")
        runtimeOnly("maven.modrinth:cultural-delights:${property("deps.cultural_delights")}")

        // Thermoo
        compileOnly("maven.modrinth:thermoo:${property("deps.thermoo")}")

        // Cold Sweat
        compileOnly("maven.modrinth:cold-sweat:${property("deps.cold_sweat")}")

        // Shield Expansion
        compileOnly("maven.modrinth:shield-expansion:${property("deps.shield_expansion")}")
        runtimeOnly("maven.modrinth:shield-expansion:${property("deps.shield_expansion")}")

        runtimeOnly("me.shedaniel.cloth:cloth-config-neoforge:${property("deps.cloth_config")}")

        gameplayMods("maven.modrinth:neapolitan:${property("deps.neapolitan")}")
        gameplayMods("maven.modrinth:blueprint:${property("deps.blueprint")}")
    }
}

fun copyGameplayModsTo(side: String) = tasks.register<Copy>("copyGameplayMods${side.replaceFirstChar(Char::uppercase)}") {
    from(gameplayMods)
    into(file("run/$side/mods"))
}

val copyGameplayModsClient = copyGameplayModsTo("client")
val copyGameplayModsServer = copyGameplayModsTo("server")

tasks {
    processResources {
        if (!isLegacy) exclude("data/modest_meals/recipe/cooking/**", "data/modest_meals/recipe/cutting/**", "data/modest_meals/recipe/chilling/**")
        exclude("**/fabric.mod.json", "**/*.accesswidener", "**/mods.toml", "$modId.fabric.mixins.json", ".cache/**")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    matching { it.name == "runClient" }.configureEach {
        dependsOn(copyGameplayModsClient)
    }

    matching { it.name == "runServer" }.configureEach {
        dependsOn(copyGameplayModsServer)
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile })
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
            artifactId = "$modId-neoforge"
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
    file = tasks.jar.map { it.archiveFile.get() }
    additionalFiles.from(tasks.named<org.gradle.jvm.tasks.Jar>("sourcesJar").map { it.archiveFile.get() })

    type = STABLE
    displayName = "${property("mod.name")} NeoForge ${stonecutter.current.project.substringBeforeLast('-')} - ${property("mod.version")}"
    version = "${property("mod.version")}+${property("deps.minecraft")}-neoforge"
    changelog = provider { rootProject.file("CHANGELOG-LATEST.md").readText() }
    modLoaders.add("neoforge")

    modrinth {
        projectId = property("publish.modrinth") as String
        accessToken = providers.environmentVariable("MODRINTH_TOKEN").orElse(providers.environmentVariable("MODRINTH_API_KEY"))
        minecraftVersions.add(property("deps.minecraft") as String)
        minecraftVersions.addAll(additionalVersions)
        requires("yacl")
        optional("raised", "reliable-remover", "reliable-gliders", "combat-roll")
        if (isLegacy) optional("farmers-delight", "cultural-delights", "emi", "thermoo", "cold-sweat", "shield-expansion")
        incompatible("melancholic_hunger")
    }

    curseforge {
        projectId = property("publish.curseforge") as String
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN").orElse(providers.environmentVariable("CURSEFORGE_API_KEY"))
        minecraftVersions.add(property("deps.minecraft") as String)
        minecraftVersions.addAll(additionalVersions)
        requires("yacl")
        optional("raised", "reliable-remover", "reliable-gliders", "combat-roll")
        if (isLegacy) optional("farmers-delight", "cultural-delights", "emi", "thermoo", "cold-sweat", "shield-expansion")
        client = true
        server = true
    }
}
