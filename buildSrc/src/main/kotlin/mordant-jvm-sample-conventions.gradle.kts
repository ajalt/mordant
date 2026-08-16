import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("mordant-kotlin-conventions")
}

kotlin {
    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        binaries {
            executable {
                mainClass.set("com.github.ajalt.mordant.samples.MainKt")
            }
        }
    }

    sourceSets {
        jvmMain.dependencies {
            implementation(project(":mordant-omnibus"))
        }
    }
}

tasks.named<CreateStartScripts>("startScriptsForJvm") {
    defaultJvmOpts = listOf("-Dfile.encoding=utf-8", "--enable-native-access=ALL-UNNAMED")
}
