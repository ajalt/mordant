plugins {
    id("mordant-jvm-sample-conventions")
    id("mordant-native-sample-conventions")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":mordant"))
        }
        jvmMain.dependencies {
            implementation(project(":mordant-omnibus"))
        }
    }
}
