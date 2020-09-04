import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`
    id("org.jetbrains.dokka")
    id("com.jfrog.bintray") version "1.8.5"
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api("com.github.ajalt:colormath:1.4.1")
    implementation("org.jetbrains:markdown:0.1.45")

    testImplementation(kotlin("test-junit"))
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.2.3")
}

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}
artifacts {
    archives(emptyJavadocJar)
}

val BINTRAY_USER: String? by project
val BINTRAY_API_KEY: String? by project
val MAVEN_USER_TOKEN: String? by project
val MAVEN_USER_PASS: String? by project
val deployDryRun = false

val versionTag = version.toString()
val githubRepo = "github.com/ajalt/mordant"
val githubUrl = "https://$githubRepo"
val scmUrl = "scm:git:git://$githubRepo.git"
val pkgDesc = "Full-featured text styling for Kotlin command-line applications"

bintray {
    user = BINTRAY_USER
    key = BINTRAY_API_KEY
    dryRun = deployDryRun // Whether to run this as dry-run, without deploying
    publish = true // If version should be auto published after an upload
    pkg.apply {
        repo = "maven"
        name = "mordant"
        userOrg = user
        publicDownloadNumbers = false
        vcsUrl = "$githubUrl.git"
        desc = pkgDesc
        websiteUrl = githubUrl
        issueTrackerUrl = "$githubUrl/issues"
        version.apply {
            name = versionTag
            desc = pkgDesc
            vcsTag = versionTag
            gpg.apply {
                sign = true // Determines whether to GPG sign the files.
            }
            mavenCentralSync.apply {
                sync = true // Optional (true by default). Determines whether to sync the version to Maven Central.
                user = MAVEN_USER_TOKEN // OSS user token
                password = MAVEN_USER_PASS // OSS user password
                close = "1" // Close staging repository and release artifacts to Maven Central. Default = 1 (true). Set to 0 = You will release the version manually.
            }
        }
    }
}
