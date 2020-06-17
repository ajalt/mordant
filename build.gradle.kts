import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm").version("1.3.72")
    id("org.jetbrains.dokka").version("0.10.1")
}

val VERSION_NAME: String by project

allprojects {
    group = "com.github.ajalt"
    version = VERSION_NAME

    repositories {
        mavenCentral()
        jcenter()
    }
}
