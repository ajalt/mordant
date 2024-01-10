plugins {
    id("mordant-mpp-sample-conventions")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":mordant"))
            }
        }
    }
}
