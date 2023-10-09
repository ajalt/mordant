include("mordant")
include("samples:detection")
include("samples:markdown")
include("samples:progress")
include("samples:table")
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
