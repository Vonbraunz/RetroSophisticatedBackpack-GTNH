import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.compiler
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlinVersion.get()}")
    }
}

plugins {
    id("java")
    id("java-library")
    kotlin("jvm") version libs.versions.kotlinVersion
    id("maven-publish")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
    id("eclipse")
    id("com.gtnewhorizons.retrofuturagradle") version "1.4.9"
}

@Suppress("PropertyName")
val mod_version: String by project
@Suppress("PropertyName")
val maven_group: String by project
@Suppress("PropertyName")
val mod_id: String by project
@Suppress("PropertyName")
val archives_base_name: String by project

@Suppress("PropertyName")
val use_access_transformer: String by project
@Suppress("PropertyName")
val use_mixins: String by project
@Suppress("PropertyName")
val use_coremod: String by project
@Suppress("PropertyName")
val use_assetmover: String by project
@Suppress("PropertyName")
val include_mod: String by project
@Suppress("PropertyName")
val coremod_plugin_class_name: String by project

version = mod_version
group = maven_group

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(JvmVendorSpec.AZUL)
    }
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

configurations {
    val embed = create("embed")
    implementation.configure {
        extendsFrom(embed)
    }
}

minecraft {
    mcVersion.set("1.7.10")
    mcpMappingChannel.set("stable")
    mcpMappingVersion.set("12")
    username.set("Developer")

    val args = mutableListOf("-ea:${group}")
    if (use_coremod.toBoolean()) {
        args += "-Dfml.coreMods.load=$coremod_plugin_class_name"
    }
    if (use_mixins.toBoolean()) {
        args += "-Dmixin.hotSwap=true"
        args += "-Dmixin.checks.interfaces=true"
        args += "-Dmixin.debug.export=true"
    }
    extraRunJvmArguments.addAll(args)
    useDependencyAccessTransformers.set(true)
    injectedTags.put("VERSION", mod_version)
    injectedTags.put("MOD_NAME", archives_base_name.replace("-", " "))
    injectedTags.put("MOD_ID", mod_id)
}

tasks.injectTags.configure {
    outputClassName.set("${maven_group}.Tags")
}

repositories {
    maven {
        name = "GTNH Maven"
        url = uri("https://nexus.gtnewhorizons.com/repository/public/")
    }
    maven {
        name = "CleanroomMC Maven"
        url = uri("https://maven.cleanroommc.com")
    }
    maven {
        name = "SpongePowered Maven"
        url = uri("https://repo.spongepowered.org/maven")
    }
    maven {
        name = "CurseMaven"
        url = uri("https://cursemaven.com")
        content { includeGroup("curse.maven") }
    }
    mavenLocal()
}

dependencies {
    // TODO: confirm the 1.7.10 Forgelin artifact and KotlinAdapter class name
    // Candidate: com.cleanroommc:forgelin-continuous:<version> (check CleanroomMC maven for 1.7.10 jar)
    // The @Mod modLanguageAdapter in RetroSophisticatedBackpacks.kt must match
    implementation("com.cleanroommc:forgelin-continuous:2.0.3") {
        exclude("net.minecraftforge")
    }

    // TODO: add ModularUI for 1.7.10 when GUI layer is implemented
    // implementation("com.cleanroommc:modularui:2.4.0")

    // TODO: confirm MixinBooter version available for 1.7.10
    if (use_mixins.toBoolean()) {
        val mixin = modUtils.enableMixins("zone.rong:mixinbooter:8.9", "mixins.${mod_id}.refmap.json") as String
        api(mixin) { isTransitive = false }
        annotationProcessor("org.ow2.asm:asm-debug-all:5.2")
        annotationProcessor("com.google.guava:guava:24.1.1-jre")
        annotationProcessor("com.google.code.gson:gson:2.8.6")
        annotationProcessor(mixin) { isTransitive = false }
    }

    // NEI for in-dev testing
    runtimeOnly("com.github.GTNewHorizons:NotEnoughItems:2.8.91-GTNH:dev")

    // Baubles removed — no bauble slot in this port
}

if (use_access_transformer.toBoolean()) {
    @Suppress("Deprecation")
    for (at in sourceSets.getByName("main").resources.files) {
        if (at.name.lowercase().endsWith("_at.cfg")) {
            tasks.deobfuscateMergedJarToSrg.get().accessTransformerFiles.from(at)
            tasks.srgifyBinpatchedJar.get().accessTransformerFiles.from(at)
        }
    }
}

tasks.withType<ProcessResources> {
    inputs.property("version", mod_version)
    inputs.property("mcversion", minecraft.mcVersion)
    filesMatching(listOf("mcmod.info", "pack.mcmeta")) {
        expand("version" to mod_version, "mcversion" to minecraft.mcVersion)
    }
    if (use_access_transformer.toBoolean()) {
        rename("(.+_at.cfg)", "META-INF/$1")
    }
}

tasks.withType<Jar> {
    manifest {
        val attributeMap = mutableMapOf<String, String>()
        if (use_coremod.toBoolean()) {
            attributeMap["FMLCorePlugin"] = coremod_plugin_class_name
            if (include_mod.toBoolean()) {
                attributeMap["FMLCorePluginContainsFMLMod"] = true.toString()
                attributeMap["ForceLoadAsMod"] = true.toString()
            }
        }
        if (use_access_transformer.toBoolean()) {
            attributeMap["FMLAT"] = "${archives_base_name}_at.cfg"
        }
        attributes(attributeMap)
    }
    from(provider {
        configurations.getByName("embed").map {
            if (it.isDirectory()) it else zipTree(it)
        }
    })
}

idea {
    module {
        inheritOutputDirs = true
    }
    project {
        settings {
            runConfigurations {
                add(Gradle("1. Run Client").apply { setProperty("taskNames", listOf("runClient")) })
                add(Gradle("2. Run Server").apply { setProperty("taskNames", listOf("runServer")) })
                add(Gradle("3. Run Obfuscated Client").apply { setProperty("taskNames", listOf("runObfClient")) })
                add(Gradle("4. Run Obfuscated Server").apply { setProperty("taskNames", listOf("runObfServer")) })
            }
            compiler.javac {
                afterEvaluate {
                    javacAdditionalOptions = "-encoding utf8"
                    moduleJavacAdditionalOptions = mutableMapOf(
                        (project.name + ".main") to tasks.compileJava.get().options.compilerArgs.joinToString(" ") { "\"$it\"" }
                    )
                }
            }
        }
    }
}

tasks.named("processIdeaSettings").configure {
    dependsOn("injectTags")
}
