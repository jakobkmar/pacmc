plugins {
    `kotlin-mp-script`
    id("org.jetbrains.compose") version "1.0.1-rc2"
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
                implementation(compose.desktop.currentOs)
                implementation(project(":pacmc-repo-api"))
                implementation("io.ktor:ktor-client-cio:1.6.7")
                implementation("br.com.devsrsouza.compose.icons.jetbrains:tabler-icons:1.0.0")
            }
        }
    }
}
