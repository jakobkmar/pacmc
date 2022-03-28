plugins {
    `kotlin-mp-script`
    application
}

kotlin {
    jvm {
        withJava()
    }

    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(project(":pacmc-app"))
                implementation(libs.clikt)
                implementation(libs.mordant)
            }
        }
    }
}

application {
    mainClass.set("net.axay.pacmc.cli.ApplicationJvmKt")
}

distributions {
    main {
        contents {
            from("$buildDir/libs") {
                rename("${project.name}-jvm", project.name)
                into("lib")
            }
        }
    }
}
