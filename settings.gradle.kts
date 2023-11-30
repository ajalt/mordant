rootProject.name = "mordant"

include(
    "mordant",
    "samples:detection",
    "samples:markdown",
    "samples:progress",
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
