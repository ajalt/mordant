@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithTests
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "1.6.0"
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.6.0")
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
                api("com.github.ajalt.colormath:colormath:3.2.0")
                implementation("org.jetbrains:markdown:0.3.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.kotest:kotest-assertions-core:5.0.2")
            }
        }

        val jvmTest by getting {
            dependencies {
                api("com.github.stefanbirkner:system-rules:1.19.0")
            }
        }

        val nativeMain by creating { dependsOn(commonMain) }
        val macosMain by creating { dependsOn(nativeMain) }
        listOf("macosX64", "macosArm64").forEach { target ->
            getByName(target + "Main").dependsOn(macosMain)
        }
        listOf("macos", "linuxX64", "mingwX64").forEach { target ->
            getByName(target + "Main").dependsOn(nativeMain)
        }

        targets.withType<KotlinNativeTargetWithTests<*>> {
            binaries {
                // Configure a separate test where code runs in background
                test("background", setOf(NativeBuildType.DEBUG)) {
                    freeCompilerArgs = freeCompilerArgs + "-trw"
                }
            }
            testRuns {
                val background by creating {
                    setExecutionSourceFrom(binaries.getByName("backgroundDebugTest") as TestExecutable)
                }
            }
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
        footerMessage = "Copyright &copy; 2021 AJ Alt"
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
