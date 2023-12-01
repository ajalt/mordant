import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("multiplatform")
}

tasks.withType<KotlinJvmCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }
    compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}
