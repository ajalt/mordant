@file:Suppress("UNUSED_VARIABLE", "KotlinRedundantDiagnosticSuppress") // https://youtrack.jetbrains.com/issue/KT-38871

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.publish)
}

kotlin {
    jvm()
    js(IR) {
        nodejs()
        browser()
    }

    linuxX64()
    mingwX64()
    val darwinTargets = listOf(
        macosArm64(),
        macosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        iosX64(),
        watchosArm64(),
        watchosSimulatorArm64(),
        watchosX64(),
        tvosArm64(),
        tvosSimulatorArm64(),
        tvosX64()
    )

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
        val darwinMain by creating { dependsOn(posixMain) }
        darwinTargets.forEach {
            getByName("${it.targetName}Main").dependsOn(darwinMain)
            getByName("${it.targetName}Test").dependsOn(commonTest)
        }
    }
}
