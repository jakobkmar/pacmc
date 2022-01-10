plugins {
    `kotlin-mp-script`
    kotlin("plugin.serialization")
    id("org.openapi.generator") version "5.3.0"
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

openApiGenerate {
    generatorName.set("kotlin")
    library.set("multiplatform")
    inputSpec.set("$projectDir/modrinth-openapi.yaml")
    outputDir.set("$buildDir/generated/modrinth-api")
    modelPackage.set("net.axay.pacmc.repoapi.modrinth.model")
}

tasks {
    this.openApiGenerate {
        doFirst {
            println("Downloading Modrinth OpenAPI file...")
            val openApiFile = file("$projectDir/modrinth-openapi.yaml")
            uri("https://docs.modrinth.com/openapi.yaml").toURL().openStream().use {
                it.copyTo(openApiFile.outputStream())
            }
            println("Finished downloading Modrinth OpenAPI file.")
        }
        doLast {
            file("$buildDir/generated/modrinth-api/src/commonMain/kotlin/net/axay")
                .copyRecursively(file("$projectDir/src/commonMain/kotlin/net/axay"), overwrite = true)
        }
    }
}
