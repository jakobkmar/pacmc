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

                val lwjglVersion = "3.3.0"
                listOf("lwjgl", "lwjgl-nfd").forEach { lwjglDep ->
                    implementation("org.lwjgl:${lwjglDep}:${lwjglVersion}")
                    listOf(
                        "natives-windows", "natives-windows-x86", "natives-windows-arm64",
                        "natives-macos", "natives-macos-arm64",
                        "natives-linux", "natives-linux-arm64", "natives-linux-arm32"
                    ).forEach { native ->
                        runtimeOnly("org.lwjgl:${lwjglDep}:${lwjglVersion}:${native}")
                    }
                }
            }
        }
    }
}
