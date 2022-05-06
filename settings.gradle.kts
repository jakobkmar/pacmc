rootProject.name = "pacmc"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

// apis
include("pacmc-repo-api")

// server-side
include("pacmc-server")
include("pacmc-server:pacmc-server-model")

// applications
include("pacmc-app")
include("pacmc-cli")
include("pacmc-gui")

// legacy
include("pacmc-cli-old")

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    versionCatalogs {
        create("libs") {
            library("kotlinx-serialization-json", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
            library("kotlinx-coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
            library("kotlinx-datetime", "org.jetbrains.kotlinx:kotlinx-datetime:0.3.3")

            version("ktor", "2.0.1")
            library("ktor-client-core", "io.ktor", "ktor-client-core").versionRef("ktor")
            library("ktor-client-contentnegotiation", "io.ktor", "ktor-client-content-negotiation").versionRef("ktor")
            library("ktor-client-cio", "io.ktor", "ktor-client-cio").versionRef("ktor")
            library("ktor-server-core", "io.ktor", "ktor-server-core").versionRef("ktor")
            library("ktor-server-netty", "io.ktor", "ktor-server-netty").versionRef("ktor")
            library("ktor-server-contentnegotiation", "io.ktor", "ktor-server-content-negotiation").versionRef("ktor")
            library("ktor-json", "io.ktor", "ktor-serialization-kotlinx-json").versionRef("ktor")

            library("kermit", "co.touchlab:kermit:1.1.1")
            library("okio", "com.squareup.okio:okio:3.1.0")

            version("realm", "0.11.1")
            library("realm-base", "io.realm.kotlin", "library-base").versionRef("realm")
            plugin("realm", "io.realm.kotlin").versionRef("realm")

            version("memoire", "0.1.0")
            library("memoire-core", "net.axay", "memoire-core").versionRef("memoire")
            library("memoire-both", "net.axay", "memoire-both").versionRef("memoire")

            version("atomicfu", "0.17.2")
            library("kotlinx-atomicfu", "org.jetbrains.kotlinx", "atomicfu").versionRef("atomicfu")
            library("kotlinx-atomicfu-plugin", "org.jetbrains.kotlinx", "atomicfu-gradle-plugin").versionRef("atomicfu")

            library("devsrsouza.icons.tablericons", "br.com.devsrsouza.compose.icons.jetbrains:tabler-icons:1.0.0")

            version("jackson", "2.13.2")
            library("jackson-kotlin", "com.fasterxml.jackson.module" , "jackson-module-kotlin").versionRef("jackson")
            library("jackson-xml", "com.fasterxml.jackson.dataformat" , "jackson-dataformat-xml").versionRef("jackson")

            library("slfj4-simple", "org.slf4j:slf4j-simple:1.7.36")
            library("jsoup", "org.jsoup:jsoup:1.14.3")

            version("kmongo", "4.5.1")
            library("kmongo", "org.litote.kmongo", "kmongo-coroutine-serialization").versionRef("kmongo")

            version("colormath", "3.2.0")
            library("colormath", "com.github.ajalt.colormath", "colormath").versionRef("colormath")
            library("colormath-compose", "com.github.ajalt.colormath.extensions", "colormath-ext-jetpack-compose").versionRef("colormath")

            library("clikt", "com.github.ajalt.clikt:clikt:3.4.2")
            library("mordant", "com.github.ajalt.mordant:mordant:2.0.0-beta5")
        }
    }
}
