import okio.HashingSink
import okio.blackholeSink
import okio.buffer
import okio.source

allprojects {
    group = "net.axay"
    version = "0.5.0"
    description = "An easy-to-use Minecraft package manager and launcher"
}

extra["kotlin.code.style"] = "official"

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.squareup.okio:okio:3.1.0")
    }
}

val githubUrl = "https://github.com/jakobkmar/pacmc"

fun requestHash(extension: String): String {
    val url = "${githubUrl}/releases/download/${version}/pacmc-${version}.${extension}"
    println("Requesting $extension sha256 hash of $url")
    return HashingSink.sha256(blackholeSink()).use { sink ->
        java.net.URL(url).openStream().source().buffer().use { source ->
            source.readAll(sink)
        }
        sink.hash.sha256().hex()
    }.let {
        println("The hash is $it")
        it
    }
}

val expandProps by lazy {
    mapOf(
        "tarHashSha256" to requestHash("tar"),
        "zipHashSha256" to requestHash("zip"),
        "javaVersion" to "11",
        "version" to version,
        "description" to description,
        "githubUrl" to githubUrl,
        "license" to "AGPL-3.0-or-later",
        "licenseArchFormat" to "AGPL3 or any later version",
    )
}

tasks {
    register<Copy>("copyPackages") {
        group = "packaging"

        from(layout.projectDirectory.dir("packages"))
        into(layout.buildDirectory.dir("packages"))

        filesMatching("**") {
            expandProps.forEach { (propertyName, propertyValue) ->
                filter { Regex("\\\${1}\\{{1}($propertyName)\\}{1}").replace(it, propertyValue.toString()) }
            }
        }
    }
}
