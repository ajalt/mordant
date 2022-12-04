@file:Suppress("UnstableApiUsage")

include("mordant")
include("samples:markdown")
include("samples:progress")
include("samples:table")


dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.7.21")
            version("jna", "5.12.1")

            plugin("dokka", "org.jetbrains.dokka").version("1.7.10")
            library("dokka-base", "org.jetbrains.dokka:dokka-base:1.7.10")

            library("colormath", "com.github.ajalt.colormath:colormath:3.2.1")
            library("markdown", "org.jetbrains:markdown:0.3.1")

            library("jna-core", "net.java.dev.jna", "jna").versionRef("jna")
            library("jna-platform", "net.java.dev.jna", "jna-platform").versionRef("jna")

            // used in tests
            library("kotest", "io.kotest:kotest-assertions-core:5.2.1")
            library("systemrules", "com.github.stefanbirkner:system-rules:1.19.0")
        }
    }
}
