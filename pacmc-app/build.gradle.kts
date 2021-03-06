plugins {
    `kotlin-mp-script`
    alias(libs.plugins.realm)
}

kotlin {
    jvm {
        withJava()
    }

    sourceSets {
        named("commonMain") {
            dependencies {
                api(project(":pacmc-common"))
                api(project(":pacmc-repo-api"))
                api(libs.okio)
                api(libs.realm.base)
                api(libs.kotlinx.datetime)
                api(libs.ktor.client.cio)
                api(libs.kotlinx.coroutines.core)
                api(libs.kermit)
                api(libs.colormath)
                implementation(libs.memoire.both)
            }
        }

        named("jvmMain") {
            dependencies {
                api("dev.dirs:directories:26")
            }
        }
    }
}
