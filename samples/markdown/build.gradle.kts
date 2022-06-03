plugins {
    application
    kotlin("jvm")
}

application {
    mainClass.set("com.github.ajalt.mordant.samples.MainKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=utf-8")
}

dependencies {
    api(kotlin("stdlib"))
    api(project(":mordant"))
}
