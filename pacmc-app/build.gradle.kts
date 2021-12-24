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
            }
        }

        named("jvmMain") {
            dependencies {
                api("dev.dirs:directories:26")
            }
        }
    }
}
