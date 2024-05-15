plugins {
    kotlin("jvm")
}

repositories {
    google()
    mavenCentral()
}

val r8: Configuration by configurations.creating

dependencies {
    implementation(project(":mordant"))
    r8(libs.r8)
}


val fatJar by tasks.register<Jar>("fatJar") {
    archiveClassifier = "fat"

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

    manifest {
        attributes["Main-Class"] = "com.github.ajalt.mordant.main.R8SmokeTestKt"
        attributes["Implementation-Version"] = archiveVersion
    }

    exclude("**/*.kotlin_metadata")
    exclude("**/*.kotlin_module")
    exclude("**/*.kotlin_builtins")
    exclude("**/module-info.class")
}


val r8JarProvider by tasks.register<JavaExec>("r8Jar") {
    dependsOn(fatJar)

    val r8File = layout.buildDirectory.file("libs/main-r8.jar")
    val rulesFile =  project.file("src/main/rules.pro")

    val fatJarFile = fatJar.archiveFile

    inputs.files(fatJarFile, rulesFile)
    outputs.file(r8File)

    classpath(r8)
    mainClass.set("com.android.tools.r8.R8")
    args = listOf(
        "--release",
        "--classfile",
        "--output", r8File.get().asFile.toString(),
        "--pg-conf", rulesFile.path,
        "--lib", System.getProperty("java.home").toString(),
        fatJarFile.get().toString(),
    )
}
