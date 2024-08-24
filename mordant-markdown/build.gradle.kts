plugins {
    id("mordant-kotlin-conventions")
    id("mordant-js-conventions")
    // Need core here pending https://github.com/JetBrains/markdown/pull/159
    id("mordant-native-core-conventions")
    id("mordant-publishing-conventions")
}

kotlin {
    jvm() // need to list explicitly since we aren't using mpp-conventions for now
    sourceSets {
        commonMain.dependencies {
            api(project(":mordant"))
            implementation(libs.markdown)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotest)
        }
    }
}
