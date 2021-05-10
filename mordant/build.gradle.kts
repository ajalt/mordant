import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("signing")
}

kotlin {
    jvm()
    js(BOTH) {
        nodejs()
        browser()
    }

    macosX64()
    linuxX64()
    mingwX64()

    sourceSets {
        all {
            with(languageSettings) {
                languageVersion = "1.5"
                apiVersion = "1.5"
                useExperimentalAnnotation("kotlin.RequiresOptIn")
            }
        }
        val gen by creating { }
        val commonMain by getting {
            dependsOn(gen)
            dependencies {
                api("com.github.ajalt.colormath:colormath:2.1.0")
                implementation("org.jetbrains:markdown:0.2.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.kotest:kotest-assertions-core:4.5.0")
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val macosX64Main by getting {
            dependsOn(nativeMain)
        }
        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }
        val mingwX64Main by getting {
            dependsOn(nativeMain)
        }

        val nativeTest by creating {
            dependsOn(commonTest)
        }
        val macosX64Test by getting {
            dependsOn(nativeTest)
        }
        val linuxX64Test by getting {
            dependsOn(nativeTest)
        }
        val mingwX64Test by getting {
            dependsOn(nativeTest)
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}
artifacts {
    archives(emptyJavadocJar)
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
