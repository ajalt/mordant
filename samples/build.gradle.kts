import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
}

application {
    mainClass.set( "com.github.ajalt.mordant.samples.MainKt")
}

dependencies {
    api(kotlin("stdlib"))
    api(project(":mordant"))
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
