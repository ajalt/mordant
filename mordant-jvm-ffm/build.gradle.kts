plugins {
    id("mordant-kotlin-conventions")
    id("mordant-publishing-conventions")
}

kotlin {
    jvm()
    jvmToolchain(22)
    sourceSets {
        commonMain.dependencies {
            implementation(project(":mordant"))
        }
        jvmMain.dependencies {
            compileOnly(libs.graalvm.svm)
        }
    }
}
