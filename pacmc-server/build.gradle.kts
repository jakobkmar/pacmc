plugins {
    `kotlin-mp-script`
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    sourceSets {
        named("commonMain") {
            dependencies {
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
                implementation(libs.kmongo.id)
            }
        }
    }
}
