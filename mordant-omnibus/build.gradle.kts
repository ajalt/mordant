plugins {
    id("mordant-mpp-conventions")
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
