plugins {
    `kotlin-mp-script`
    application
}

buildscript {
    dependencies {
        classpath(libs.kotlinx.atomicfu.plugin)
    }
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
                compileOnly(libs.kotlinx.atomicfu)
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
