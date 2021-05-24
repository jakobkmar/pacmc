import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
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
