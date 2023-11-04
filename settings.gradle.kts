include("mordant")
include("samples:detection")
include("samples:markdown")
include("samples:progress")
include("samples:table")
include("samples:tour")
include("test:graalvm")
include("test:proguard")


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
