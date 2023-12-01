plugins {
    alias(libs.plugins.kotlinBinaryCompatibilityValidator)
}

apiValidation {
    // https://github.com/Kotlin/binary-compatibility-validator/issues/3
    project("samples").subprojects.mapTo(ignoredProjects) { it.name }
    project("test").subprojects.mapTo(ignoredProjects) { it.name }
}
