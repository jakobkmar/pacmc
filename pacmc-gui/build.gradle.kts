plugins {
    `kotlin-mp-script`
    id("org.jetbrains.compose") version "1.0.1"
}

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

kotlin {
    jvm()

    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation(project(":pacmc-app"))

                implementation(compose.desktop.currentOs)
                implementation("br.com.devsrsouza.compose.icons.jetbrains:tabler-icons:1.0.0")
            }
        }
    }
}
