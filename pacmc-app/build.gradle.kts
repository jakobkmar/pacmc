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
                api("com.squareup.okio:okio:3.0.0")
                api("io.realm.kotlin:library-base:0.8.0")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
                api("io.ktor:ktor-client-cio:1.6.7")
            }
        }

        named("jvmMain") {
            dependencies {
                api("dev.dirs:directories:26")
            }
        }
    }
}
