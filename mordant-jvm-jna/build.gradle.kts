plugins {
    id("mordant-kotlin-conventions")
    id("mordant-publishing-conventions")
}

kotlin {
    jvm()
    sourceSets {
        commonMain.dependencies {
            implementation(project(":mordant"))
        }
        jvmMain.dependencies {
            implementation(libs.jna.core)
        }
    }
}
