import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("mordant-native-conventions")
}

kotlin.targets.filterIsInstance<KotlinNativeTarget>().forEach { target ->
    target.binaries.executable {
        entryPoint = "com.github.ajalt.mordant.samples.main"
    }
}
