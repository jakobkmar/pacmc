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

// applications
include("pacmc-app")
include("pacmc-cli")
include("pacmc-gui")

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    versionCatalogs {
        create("libs") {
            alias("kotlinx-coroutines-core").to("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
            alias("kotlinx-datetime").to("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")

            version("ktor", "1.6.7")
            alias("ktor-client-core").to("io.ktor", "ktor-client-core").versionRef("ktor")
            alias("ktor-client-serialization").to("io.ktor", "ktor-client-serialization").versionRef("ktor")
            alias("ktor-client-cio").to("io.ktor", "ktor-client-cio").versionRef("ktor")
            alias("ktor-server-core").to("io.ktor", "ktor-server-core").versionRef("ktor")
            alias("ktor-server-netty").to("io.ktor", "ktor-server-netty").versionRef("ktor")
            alias("ktor-server-serialization").to("io.ktor", "ktor-serialization").versionRef("ktor")

            alias("kermit").to("co.touchlab:kermit:1.0.0")
            alias("okio").to("com.squareup.okio:okio:3.0.0")
            alias("realm-base").to("io.realm.kotlin:library-base:0.8.0")

            alias("devsrsouza.icons.tablericons").to("br.com.devsrsouza.compose.icons.jetbrains:tabler-icons:1.0.0")

            version("jackson", "2.13.1")
            alias("jackson-kotlin").to("com.fasterxml.jackson.module" , "jackson-module-kotlin").versionRef("jackson")
            alias("jackson-xml").to("com.fasterxml.jackson.dataformat" , "jackson-dataformat-xml").versionRef("jackson")

            alias("slfj4-simple").to("org.slf4j:slf4j-simple:1.7.32")
            alias("jsoup").to("org.jsoup:jsoup:1.14.3")

            version("kmongo", "4.4.0")
            alias("kmongo").to("org.litote.kmongo", "kmongo-coroutine-serialization").versionRef("kmongo")
            alias("kmongo-id").to("org.litote.kmongo", "kmongo-id-serialization").versionRef("kmongo")
        }
    }
}
