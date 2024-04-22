import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask

plugins {
    kotlin("multiplatform")
}

kotlin {
    js {
        // We have different code paths on browsers and node, so we run tests on both
        nodejs()
        browser()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
        browser()
    }

    sourceSets {
        val jsCommonMain by creating { dependsOn(commonMain.get()) }
        jsMain.get().dependsOn(jsCommonMain)
        getByName("wasmJsMain").dependsOn(jsCommonMain)
    }
}

// Need to compile using a canary version of Node due to
// https://youtrack.jetbrains.com/issue/KT-63014
rootProject.the<NodeJsRootExtension>().apply {
    nodeVersion = "21.0.0-v8-canary2023091837d0630120"
    nodeDownloadBaseUrl = "https://nodejs.org/download/v8-canary"
}
