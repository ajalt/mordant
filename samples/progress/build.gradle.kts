plugins {
    id("mordant-jvm-sample-conventions")
}

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":mordant-coroutines"))
        }
    }
}
