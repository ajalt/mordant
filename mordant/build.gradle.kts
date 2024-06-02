plugins {
    id("mordant-mpp-conventions")
    id("mordant-publishing-conventions")
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlin.experimental.ExperimentalNativeApi")
        }
        commonMain.dependencies {
            api(libs.colormath)
            implementation(libs.markdown)
            implementation(libs.jna.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotest)
        }
        jvmMain.dependencies {
            compileOnly(libs.graalvm.svm)
        }
        jvmTest.dependencies {
            api(libs.systemrules)
        }
    }
}
