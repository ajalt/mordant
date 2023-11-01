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

    macosX64()
    macosArm64()
    linuxX64()
    mingwX64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
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

        val jvmMain by getting {
            dependencies {
                compileOnly(libs.graalvm.svm)
            }
        }

        val jvmTest by getting {
            dependencies {
                api(libs.systemrules)
            }
        }

        val posixMain by creating { dependsOn(nativeMain.get()) }
        linuxMain.get().dependsOn(posixMain)
        macosMain.get().dependsOn(posixMain)
    }
}
