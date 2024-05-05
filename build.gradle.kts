import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask

plugins {
    alias(libs.plugins.kotlinBinaryCompatibilityValidator)
    id("org.jetbrains.dokka")
}

apiValidation {
    // https://github.com/Kotlin/binary-compatibility-validator/issues/3
    project("samples").subprojects.mapTo(ignoredProjects) { it.name }
    project("test").subprojects.mapTo(ignoredProjects) { it.name }
}

tasks.withType<DokkaMultiModuleTask>().configureEach {
    outputDirectory.set(rootProject.rootDir.resolve("docs/api"))
    pluginsMapConfiguration.set(
        mapOf(
            "org.jetbrains.dokka.base.DokkaBase" to """{
                "footerMessage": "Copyright &copy; 2017 AJ Alt"
            }"""
        )
    )
}

// https://youtrack.jetbrains.com/issue/KT-63014
tasks.withType<KotlinNpmInstallTask>().configureEach {
    args.add("--ignore-engines")
}
