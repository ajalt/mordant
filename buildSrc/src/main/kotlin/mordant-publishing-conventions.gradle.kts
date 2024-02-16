@file:Suppress("PropertyName")

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.io.ByteArrayOutputStream

plugins {
    id("com.vanniktech.maven.publish.base")
    id("org.jetbrains.dokka")
}

fun getPublishVersion(): String {
    val version = project.property("VERSION_NAME").toString()
    // Call gradle with -PinferVersion to set the dynamic version name.
    // Otherwise, we skip it to save time.
    if (!project.hasProperty("inferVersion")) return version

    val stdout = ByteArrayOutputStream()
    project.exec {
        commandLine = listOf("git", "tag", "--points-at", "master")
        standardOutput = stdout
    }
    val tag = String(stdout.toByteArray()).trim()
    if (tag.isNotEmpty()) return tag

    val buildNumber = System.getenv("GITHUB_RUN_NUMBER") ?: "0"
    return "$version.$buildNumber-SNAPSHOT"
}

// Since we want to set the version name dynamically, we have to use the base plugin
// https://github.com/vanniktech/gradle-maven-publish-plugin/issues/624
@Suppress("UnstableApiUsage")
mavenPublishing {
    project.setProperty("VERSION_NAME", getPublishVersion())
    pomFromGradleProperties()
    configure(KotlinMultiplatform(JavadocJar.Dokka("dokkaHtmlPartial")))
    publishToMavenCentral(SonatypeHost.DEFAULT)
    signAllPublications()
}

tasks.withType<DokkaTaskPartial>().configureEach {
    dokkaSourceSets.configureEach {
        skipDeprecated.set(true)
    }
}

