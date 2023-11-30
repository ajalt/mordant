plugins {
    id("mordant-kotlin-conventions")
    application
}

kotlin {
    // For some reason MainKt isn't present in the jar unless we add withJava
    jvm { withJava() }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":mordant"))
            }
        }
    }
}

application {
    mainClass.set("com.github.ajalt.mordant.samples.MainKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=utf-8")
}
