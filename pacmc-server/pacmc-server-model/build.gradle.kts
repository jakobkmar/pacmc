plugins {
    `kotlin-mp-script`
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

    sourceSets {
        named("commonMain") {
            dependencies {
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.datetime)
            }
        }
    }
}
