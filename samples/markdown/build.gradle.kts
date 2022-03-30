plugins {
    application
    kotlin("jvm")
}

application {
    mainClass.set("com.github.ajalt.mordant.samples.MainKt")
}

dependencies {
    api(kotlin("stdlib"))
    api(project(":mordant"))
}
