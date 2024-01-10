plugins {
    id("mordant-mpp-conventions")
    id("mordant-publishing-conventions")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.colormath)
                implementation(libs.markdown)
                implementation(libs.jna.core)
                implementation(project.dependencies.platform(libs.kotlinxCoroutines.bom))
                implementation(libs.kotlinxCoroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotest)

                implementation(libs.kotlinxCoroutines.test)
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
    }
}
