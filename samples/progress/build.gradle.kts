plugins {
    id("mordant-mpp-sample-conventions")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":mordant"))
                implementation(project.dependencies.platform(libs.kotlinxCoroutines.bom))
                implementation(libs.kotlinxCoroutines.core)
            }
        }
    }
}
