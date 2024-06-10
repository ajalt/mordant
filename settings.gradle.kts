rootProject.name = "mordant"

include(
    "mordant",
    "extensions:mordant-coroutines",
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
