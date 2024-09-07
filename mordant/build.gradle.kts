plugins {
    id("mordant-mpp-conventions")
    id("mordant-publishing-conventions")
}

kotlin {
    jvm()
    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlin.experimental.ExperimentalNativeApi")
        }
        commonMain.dependencies {
            api(libs.colormath)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotest)
        }
        jvmTest.dependencies {
            api(libs.systemrules)
        }
        // Kotlin 2.0 changed the way MPP is compiled, so instead of copying shared sources to each
        // target, it compiles intermediate sources separately. That means that code that previously
        // compiled is broken due to errors like "declaration is using numbers with different bit
        // widths". So we copy the shared sources to each target manually.
        sourceSets {
            // https://kotlinlang.org/docs/multiplatform-hierarchy.html#see-the-full-hierarchy-template
            val posixMain by creating { dependsOn(nativeMain.get()) }
            linuxMain.get().dependsOn(posixMain)
            appleMain.get().dependsOn(posixMain)
            val appleNonDesktopMain by creating { dependsOn(appleMain.get()) }
            for (target in listOf(iosMain, tvosMain, watchosMain)) {
                target.get().dependsOn(appleNonDesktopMain)
            }
            for (target in listOf(
                "linuxX64", "linuxArm64",
                "macosX64", "macosArm64",
                "tvosX64", "tvosArm64", "tvosSimulatorArm64",
                "watchosArm32", "watchosArm64", "watchosX64", "watchosSimulatorArm64",
            )) {
                sourceSets.getByName(target + "Main").kotlin.srcDirs("src/posixSharedMain/kotlin")
            }
        }
    }
}
