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
