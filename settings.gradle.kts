rootProject.name = "pacmc"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

// apis
include("pacmc-repo-api")

// applications
include("pacmc-app")
include("pacmc-cli")
include("pacmc-gui")

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    versionCatalogs {
        create("libs") {
            version("ktor", "1.6.7")
            alias("ktor-client-core").to("io.ktor", "ktor-client-core").versionRef("ktor")
            alias("ktor-client-serialization").to("io.ktor", "ktor-client-serialization").versionRef("ktor")
            alias("ktor-client-cio").to("io.ktor", "ktor-client-cio").versionRef("ktor")
            alias("kotlinx-coroutines-core").to("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
            alias("kotlinx-datetime").to("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
            alias("kermit").to("co.touchlab:kermit:1.0.0")
            alias("okio").to("com.squareup.okio:okio:3.0.0")
            alias("realm-base").to("io.realm.kotlin:library-base:0.8.0")
            alias("devsrsouza.icons.tablericons").to("br.com.devsrsouza.compose.icons.jetbrains:tabler-icons:1.0.0")
        }
    }
}
