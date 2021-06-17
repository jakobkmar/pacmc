import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.10"
    application
}

group = "net.axay"
version = "1.0-SNAPSHOT"

description = "An easy to use package manager for Fabric Minecraft mods."

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.github.ajalt.clikt:clikt:3.2.0")
    implementation("com.github.ajalt.mordant:mordant:2.0.0-beta1")

    implementation("io.ktor:ktor-client-core:1.6.0")
    implementation("io.ktor:ktor-client-cio:1.6.0")
    implementation("io.ktor:ktor-client-serialization:1.6.0")

    implementation("org.jetbrains.xodus:xodus-openAPI:1.3.232")
    implementation("org.jetbrains.xodus:xodus-environment:1.3.232")
    implementation("org.jetbrains.xodus:xodus-entity-store:1.3.232")

    implementation("org.slf4j:slf4j-simple:1.7.30")

    implementation("dev.dirs:directories:26")
}

tasks {
    withType<JavaCompile> {
        options.release.set(11)
        options.encoding = "UTF-8"
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}

application {
    mainClass.set("net.axay.pacmc.ManagerKt")
}
