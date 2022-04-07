import net.axay.openapigenerator.OpenApiGenerateTask

plugins {
    `kotlin-mp-script`
    kotlin("plugin.serialization")
    id("net.axay.openapigenerator") version "0.1.2"
}

buildscript {
    dependencies {
        classpath("net.axay:openapigenerator-jvm:0.0.1")
    }
}

kotlin {
    jvm()

    sourceSets {
        named("commonMain") {
            dependencies {
                api(libs.ktor.client.core)
                api(libs.ktor.client.serialization)
                api(libs.kotlinx.datetime)
            }
        }
    }
}

tasks {
    register<OpenApiGenerateTask>("generateModrinthApi") {
        group = "generate"

        specUrl.set("https://docs.modrinth.com/openapi.yaml")
        outputDirectory.set(file("src/commonMain/kotlin"))
        packageName.set("net.axay.pacmc.repoapi.modrinth.model")
    }
}
