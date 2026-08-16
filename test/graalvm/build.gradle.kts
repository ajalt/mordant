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
    // The junit version that comes with kotlin-test is too old to track tests skipped on the JVM,
    // which the native-image junit feature requires
    testImplementation(platform(libs.junit.bom))
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
