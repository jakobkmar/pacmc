import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath("commons-codec:commons-codec:1.15")
    }
}

plugins {
    kotlin("jvm") version "1.5.21"
    kotlin("plugin.serialization") version "1.5.21"
    application
}

group = "net.axay"
version = "0.4.0"

description = "An easy to use package manager for Fabric Minecraft mods."

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.github.ajalt.clikt:clikt:3.2.0")
    implementation("com.github.ajalt.mordant:mordant:2.0.0-beta2")

    implementation("io.ktor:ktor-client-core:1.6.1")
    implementation("io.ktor:ktor-client-cio:1.6.1")
    implementation("io.ktor:ktor-client-serialization:1.6.1")

    implementation("dev.dirs:directories:26")
    implementation("org.apache.commons:commons-text:1.9")

    implementation("org.slf4j:slf4j-simple:1.7.30")

    implementation("org.kodein.db:kodein-db-jvm:0.8.1-beta")
    implementation("org.kodein.db:kodein-db-serializer-kotlinx:0.8.1-beta")
    implementation("org.kodein.db:kodein-leveldb-jni-jvm:0.8.1-beta")
}

application {
    mainClass.set("net.axay.pacmc.ManagerKt")
}

sourceSets {
    create("packages") {
        output.setResourcesDir(rootDir.resolve("packages/"))
    }
}

tasks {
    withType<JavaCompile> {
        options.release.set(11)
        options.encoding = "UTF-8"
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    processResources {
        inputs.property("version", version)

        filesMatching("pacmc_version.txt") {
            expand("version" to version)
        }
    }

    val packagesResources = named<ProcessResources>(sourceSets["packages"].processResourcesTaskName) {
        dependsOn(distTar)

        val distTarOutput = distTar.get().outputs.files.firstOrNull { it.extension == "tar" }
            ?.let { if (it.exists()) it.inputStream() else null }

        val propertyMap = mapOf(
            "version" to project.version,
            "javaVersion" to this@tasks.compileJava.get().options.release.get(),
            "sha256Hash" to (if (distTarOutput != null) org.apache.commons.codec.digest.DigestUtils.sha256Hex(distTarOutput) else null)
                .apply { logger.quiet("The distTar file hash is $this") },
            "description" to project.description
        )

        inputs.properties(propertyMap)

        filesMatching("aur/pacmc/PKGBUILD") {
            propertyMap.forEach { (propertyName, propertyValue) ->
                filter { Regex("\\\${1}\\{{1}($propertyName)\\}{1}").replace(it, propertyValue.toString()) }
            }
        }
    }

    val systemVersion = File("/proc/version").let { if (it.exists()) it.readText().toLowerCase() else "" }

    if (systemVersion.contains("arch") || systemVersion.contains("manjaro")) {
        create<Exec>("updateAurPackage") {
            group = "packages"

            dependsOn(packagesResources)

            workingDir("packages/aur/pacmc/")

            standardOutput = workingDir.resolve(".SRCINFO").outputStream()
            commandLine("makepkg", "--printsrcinfo")
        }

        create<Exec>("commitAurPackage") {
            group = "packages"

            workingDir("packages/aur/pacmc/")

            commandLine("git", "add", "PKGBUILD", ".SRCINFO")
            commandLine("git", "commit", "-m", "\"Update pacmc to version $version\"")
        }
    }
}
