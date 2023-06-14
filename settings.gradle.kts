@file:Suppress("UnstableApiUsage")

include("mordant")
include("samples:markdown")
include("samples:progress")
include("samples:table")


dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.8.22")

            plugin("dokka", "org.jetbrains.dokka").version("1.8.10")
            plugin("publish", "com.vanniktech.maven.publish").version("0.25.2")

            library("colormath", "com.github.ajalt.colormath:colormath:3.2.1")
            library("markdown", "org.jetbrains:markdown:0.4.1")
            library("jna-core", "net.java.dev.jna:jna:5.13.0")

            // used in tests
            library("kotest", "io.kotest:kotest-assertions-core:5.5.4")
            library("systemrules", "com.github.stefanbirkner:system-rules:1.19.0")
        }
    }
}
