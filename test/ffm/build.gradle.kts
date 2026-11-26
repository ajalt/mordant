import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":mordant"))
    implementation(project(":mordant-jvm-ffm"))
}

kotlin.compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

application {
    mainClass.set("FfmSmokeTestKt")
}
