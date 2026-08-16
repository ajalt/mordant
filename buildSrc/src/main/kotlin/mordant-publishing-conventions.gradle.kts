@file:Suppress("PropertyName")

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform

plugins {
    id("com.vanniktech.maven.publish.base")
    id("org.jetbrains.dokka")
}

fun getPublishVersion(): String {
    val version = project.property("VERSION_NAME").toString()
    // Call gradle with -PsnapshotVersion to set the version as a snapshot.
    if (!project.hasProperty("snapshotVersion")) return version
    val buildNumber = System.getenv("GITHUB_RUN_NUMBER") ?: "0"
    return "$version.$buildNumber-SNAPSHOT"
}

// Since we want to set the version name dynamically, we have to use the base plugin
@Suppress("UnstableApiUsage")
mavenPublishing {
    project.setProperty("VERSION_NAME", getPublishVersion())
    pomFromGradleProperties()
    configure(KotlinMultiplatform(JavadocJar.Empty()))
    publishToMavenCentral()
    signAllPublications()
}

dokka {
    dokkaSourceSets.configureEach {
        skipDeprecated.set(true)
    }
}
