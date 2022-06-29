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
                api(project(":pacmc-common"))
                api(libs.ktor.client.core)
                api(libs.ktor.client.contentnegotiation)
                api(libs.ktor.json)
                api(libs.kotlinx.datetime)
                api(libs.memoire.core)
            }
        }
    }
}

tasks {
    register<OpenApiGenerateTask>("generateModrinthApi") {
        group = "generate"

        deleteOldOutput.set(true)

        specUrl.set("https://docs.modrinth.com/openapi.yaml")
        outputDirectory.set(file("src/commonMain/kotlin"))
        packageName.set("net.axay.pacmc.repoapi.modrinth.model")
    }
}
