rootProject.name = "mordant"

include(
    "mordant",
    "mordant-omnibus",
    "mordant-jvm-jna",
    "mordant-jvm-ffm",
    "mordant-jvm-graal-ffi",
    "mordant-coroutines",
    "mordant-markdown",
    "samples:detection",
    "samples:drawing",
    "samples:hexviewer",
    "samples:markdown",
    "samples:progress",
    "samples:select",
    "samples:table",
    "samples:tour",
    "test:graalvm",
    "test:proguard",
)

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
