import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
}

group = "net.axay"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}

tasks {
    withType<JavaCompile> {
        options.release.set(11)
        options.encoding = "UTF-8"
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}
