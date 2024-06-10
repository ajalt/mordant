plugins {
    id("mordant-mpp-sample-conventions")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":extensions:mordant-coroutines"))
        }
    }
}
