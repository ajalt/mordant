plugins {
    id("mordant-kotlin-conventions")
    id("mordant-native-conventions")
    id("mordant-js-conventions")
    id("mordant-publishing-conventions")
}

kotlin {
    jvm()
    sourceSets {
        commonMain.dependencies {
            api(project(":mordant"))
        }
        jvmMain.dependencies {
            implementation(project(":mordant-jvm-jna"))
            implementation(project(":mordant-jvm-ffm"))
            implementation(project(":mordant-jvm-graal-ffi"))
        }
    }
}
