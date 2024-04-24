rootProject.name = "mordant"

include(
    "mordant",
    "extensions:mordant-coroutines",
    "samples:detection",
    "samples:hexviewer",
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
        // TODO: we can remove this once kotest releases a new version
        maven {
            url= uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            mavenContent { snapshotsOnly() }
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
