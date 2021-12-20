plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
    }
}
