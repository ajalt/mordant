@file:Suppress("UnstableApiUsage")

include("mordant")
include("samples:markdown")
include("samples:progress")
include("samples:table")


dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.7.0")

            plugin("dokka", "org.jetbrains.dokka").version("1.6.21")

            library("dokka-base", "org.jetbrains.dokka:dokka-base:1.6.21")
            library("colormath", "com.github.ajalt.colormath:colormath:3.2.0")
            library("markdown", "org.jetbrains:markdown:0.3.1")

            // used in tests
            library("kotest", "io.kotest:kotest-assertions-core:5.2.1")
            library("systemrules", "com.github.stefanbirkner:system-rules:1.19.0")

            // used in samples
            library("kodein", "org.kodein.di:kodein-di-generic-jvm:5.2.0")
            library("kotlinx-serialization", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
            library("mordant", "com.github.ajalt:mordant:1.2.1")
        }
    }
}
