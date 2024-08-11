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
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotest)
        }
        jvmTest.dependencies {
            api(libs.systemrules)
        }
    }
}
