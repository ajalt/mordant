plugins {
    id("mordant-jvm-sample-conventions")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":extensions:mordant-coroutines"))
        }
    }
}
