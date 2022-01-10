plugins {
    `kotlin-mp-script`
    id("io.realm.kotlin") version "0.8.0"
}

kotlin {
    jvm {
        withJava()
    }

    sourceSets {
        named("commonMain") {
            dependencies {
                api(project(":pacmc-repo-api"))
                api(libs.okio)
                api(libs.realm.base)
                api(libs.kotlinx.datetime)
                api(libs.ktor.client.cio)
                api(libs.kotlinx.coroutines.core)
                api(libs.kermit)
            }
        }

        named("jvmMain") {
            dependencies {
                api("dev.dirs:directories:26")
            }
        }
    }
}
