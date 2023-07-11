plugins {
    application
    kotlin("jvm")
    id("org.graalvm.buildtools.native") version "0.9.23"
}

application {
    mainClass.set("com.github.ajalt.mordant.samples.MainKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=utf-8")
}

dependencies {
    api(kotlin("stdlib"))
    api(project(":mordant"))
}

