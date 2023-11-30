@file:Suppress("PropertyName")

import org.jetbrains.dokka.gradle.DokkaTask
import org.gradle.kotlin.dsl.provideDelegate
import java.io.ByteArrayOutputStream

plugins {
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
}

val VERSION_NAME: String by project

fun getPublishVersion(): String {
    // Call gradle with -PinferVersion to set the dynamic version name.
    // Otherwise, we skip it to save time.
    if (!project.hasProperty("inferVersion")) return VERSION_NAME

    val stdout = ByteArrayOutputStream()
    project.exec {
        commandLine = listOf("git", "tag", "--points-at", "master")
        standardOutput = stdout
    }
    val tag = String(stdout.toByteArray()).trim()
    if (tag.isNotEmpty()) return tag

    val buildNumber = System.getenv("GITHUB_RUN_NUMBER") ?: "0"
    return "$VERSION_NAME.$buildNumber-SNAPSHOT"
}

project.setProperty("VERSION_NAME", getPublishVersion())

tasks.named<DokkaTask>("dokkaHtml") {
    outputDirectory.set(rootProject.rootDir.resolve("docs/api"))
    pluginsMapConfiguration.set(
        mapOf(
            "org.jetbrains.dokka.base.DokkaBase" to """{
                "footerMessage": "Copyright &copy; 2017 AJ Alt"
            }"""
        )
    )
    dokkaSourceSets.configureEach {
        reportUndocumented.set(false)
        skipDeprecated.set(true)
    }
}
