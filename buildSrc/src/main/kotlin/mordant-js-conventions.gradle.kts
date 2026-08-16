import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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
        val jsCommonMain = create("jsCommonMain") { dependsOn(commonMain.get()) }
        jsMain.get().dependsOn(jsCommonMain)
        wasmJsMain.get().dependsOn(jsCommonMain)
    }
}
