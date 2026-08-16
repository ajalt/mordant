plugins {
    alias(libs.plugins.kotlinBinaryCompatibilityValidator)
    id("org.jetbrains.dokka")
}

apiValidation {
    // https://github.com/Kotlin/binary-compatibility-validator/issues/3
    project("samples").subprojects.mapTo(ignoredProjects) { it.name }
    project("test").subprojects.mapTo(ignoredProjects) { it.name }
}

dependencies {
    dokka(project(":mordant"))
    dokka(project(":mordant-coroutines"))
    dokka(project(":mordant-jvm-ffm"))
    dokka(project(":mordant-jvm-graal-ffi"))
    dokka(project(":mordant-jvm-jna"))
    dokka(project(":mordant-markdown"))
    dokka(project(":mordant-omnibus"))
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(rootDir.resolve("docs/api"))
    }
    pluginsConfiguration.html {
        footerMessage.set("Copyright &copy; 2017 AJ Alt")
    }
}
