plugins {
    kotlin("multiplatform")
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
            dependencies {
                api(project(":mordant"))
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
    }
}
