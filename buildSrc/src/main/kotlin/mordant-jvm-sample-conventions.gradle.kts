plugins {
    id("mordant-kotlin-conventions")
    application
}

kotlin {
    // For some reason MainKt isn't present in the jar unless we add withJava
    jvm { withJava() }

    sourceSets {
        jvmMain.dependencies {
            implementation(project(":mordant-omnibus"))
        }
    }
}

application {
    mainClass.set("com.github.ajalt.mordant.samples.MainKt")
    applicationDefaultJvmArgs = listOf(
        "-Dfile.encoding=utf-8",
        "--enable-native-access=ALL-UNNAMED"
    )
}
