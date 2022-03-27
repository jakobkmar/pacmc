plugins {
    `kotlin-mp-script`
    id("org.jetbrains.compose") version "1.1.1"
}

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

kotlin {
    jvm()

    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(project(":pacmc-server:pacmc-server-model"))
                implementation(project(":pacmc-app"))
            }
        }

        named("jvmMain") {
            dependencies {
                implementation(compose.desktop.currentOs)

                implementation(libs.devsrsouza.icons.tablericons)
                implementation(libs.colormath)
                implementation(libs.colormath.compose)

                val lwjglVersion = "3.3.1"
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
