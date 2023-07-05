@file:Suppress("UNUSED_VARIABLE", "KotlinRedundantDiagnosticSuppress") // https://youtrack.jetbrains.com/issue/KT-38871

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.publish)
    `java-library`
}

kotlin {
    jvm()
    js(IR) {
        nodejs()
        browser()
    }

    macosX64()
    macosArm64()
    linuxX64()
    mingwX64()

    sourceSets {
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
