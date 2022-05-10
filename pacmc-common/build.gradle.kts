plugins {
    `kotlin-mp-script`
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.okio)
            }
        }
    }
}
