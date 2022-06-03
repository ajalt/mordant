import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    application
}

kotlin {
    jvm()
    macosX64()
    linuxX64()
    mingwX64()

    targets.filterIsInstance<KotlinNativeTarget>().forEach { target ->
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
    }
}

application {
    mainClass.set("com.github.ajalt.mordant.samples.MainKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=utf-8")
}
