import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    application
}

kotlin {
    jvm()
    macosX64()
    linuxX64()
    mingwX64()

    targets.filterIsInstance<KotlinNativeTarget>()
            .forEach { target ->
                target.binaries.executable {
                    entryPoint = "com.github.ajalt.mordant.samples.main"
                }
            }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":mordant"))
            }
        }

        val jvmMain by getting
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
    }
}

application {
    mainClass.set("com.github.ajalt.mordant.samples.MainKt")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
