plugins {
    kotlin("jvm")
    alias(libs.plugins.graalvm.nativeimage)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":mordant-omnibus"))
    implementation(project(":mordant-markdown"))
    testImplementation(kotlin("test"))
}

graalvmNative {
    binaries {
        named("test") {
            quickBuild.set(true)
            buildArgs(
                // https://github.com/oracle/graal/issues/6957
                "--initialize-at-build-time=kotlin.annotation.AnnotationTarget,kotlin.annotation.AnnotationRetention",
            )
        }
    }
}
