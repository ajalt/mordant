include("mordant")
include("samples:detection")
include("samples:markdown")
include("samples:progress")
include("samples:table")
include("graalvm")


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
