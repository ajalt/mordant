import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

// We don't use all the conventions plugins here since applying the mpp=convention results in
// "IllegalStateException: Configuration already finalized for previous property values"
plugins {
    kotlin("multiplatform")
    id("mordant-native-conventions")
    id("mordant-publishing-conventions")
}

kotlin {
    jvm()
    js { nodejs() } // we don't have any code, but it's an error not to pick aa JS environment
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { nodejs() }
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
