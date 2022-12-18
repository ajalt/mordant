@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    kotlin("multiplatform")
    alias(libs.plugins.dokka)
    id("maven-publish")
    id("signing")
}


buildscript {
    dependencies {
        classpath(libs.dokka.base)
    }
}

kotlin {
    jvm()
    js(BOTH) {
        nodejs()
        browser()
    }

    macosX64()
    macosArm64()
    linuxX64()
    mingwX64()

    sourceSets {
        all {
            with(languageSettings) {
                languageVersion = "1.6"
                apiVersion = "1.6"
                optIn("kotlin.RequiresOptIn")
            }
        }
        val gen by creating { }
        val commonMain by getting {
            dependsOn(gen)
            dependencies {
                api(libs.colormath)
                implementation(libs.markdown)
                implementation(libs.jna.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotest)
            }
        }

        val jvmTest by getting {
            dependencies {
                api(libs.systemrules)
            }
        }

        val nativeMain by creating { dependsOn(commonMain) }
        val mingwX64Main by getting { dependsOn(nativeMain) }
        val posixMain by creating { dependsOn(nativeMain) }
        val linuxX64Main by getting { dependsOn(posixMain) }
        val macosMain by creating { dependsOn(posixMain) }
        listOf("macosX64", "macosArm64").forEach { target ->
            getByName(target + "Main").dependsOn(macosMain)
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.dokkaHtml.configure {
    outputDirectory.set(rootDir.resolve("docs/api"))
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        footerMessage = "Copyright &copy; 2022 AJ Alt"
    }
    dokkaSourceSets {
        configureEach {
            reportUndocumented.set(false)
            skipDeprecated.set(true)
        }
    }
}

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}


val isSnapshot = version.toString().endsWith("SNAPSHOT")
val signingKey: String? by project
val SONATYPE_USERNAME: String? by project
val SONATYPE_PASSWORD: String? by project

publishing {
    publications.withType<MavenPublication>().all {
        pom {
            description.set("Colorful multiplatform styling Kotlin for command-line applications")
            name.set("Mordant")
            url.set("https://github.com/ajalt/mordant")
            scm {
                url.set("https://github.com/ajalt/mordant")
                connection.set("scm:git:git://github.com/ajalt/mordant.git")
                developerConnection.set("scm:git:ssh://git@github.com/ajalt/mordant.git")
            }
            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }
            developers {
                developer {
                    id.set("ajalt")
                    name.set("AJ Alt")
                    url.set("https://github.com/ajalt")
                }
            }
        }
    }

    repositories {
        val releaseUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
        val snapshotUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
        maven {
            url = if (isSnapshot) snapshotUrl else releaseUrl
            credentials {
                username = SONATYPE_USERNAME ?: ""
                password = SONATYPE_PASSWORD ?: ""
            }
        }
    }

    publications.withType<MavenPublication>().all {
        artifact(emptyJavadocJar.get())
    }
}

signing {
    isRequired = !isSnapshot

    if (signingKey != null && !isSnapshot) {
        useInMemoryPgpKeys(signingKey, "")
        sign(publishing.publications)
    }
}
