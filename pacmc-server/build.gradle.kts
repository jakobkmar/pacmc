plugins {
    `kotlin-mp-script`
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    sourceSets {
        named("jvmMain") {
            dependencies {
                api(project(":pacmc-server:pacmc-server-model"))

                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.netty)
                implementation(libs.ktor.server.serialization)

                implementation(libs.kotlinx.datetime)

                implementation(libs.slfj4.simple)
                implementation(libs.kermit)
                implementation(libs.jackson.kotlin)
                implementation(libs.jackson.xml)
                implementation(libs.jsoup)

                implementation(libs.kmongo)
            }
        }
    }
}
