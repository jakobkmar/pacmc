plugins {
    `kotlin-mp-script`
    id("io.realm.kotlin") version "0.8.0"
}

kotlin {
    jvm()

    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(project(":pacmc-repo-api"))
                implementation("com.squareup.okio:okio:3.0.0")
                implementation("io.realm.kotlin:library-base:0.8.0")
            }
        }

        named("jvmMain") {
            dependencies {
                implementation("dev.dirs:directories:26")
            }
        }
    }
}
