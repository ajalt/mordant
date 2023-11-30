plugins {
    kotlin("multiplatform")
}

kotlin {
    macosX64()
    macosArm64()
    linuxX64()
    mingwX64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val posixMain by creating { dependsOn(nativeMain.get()) }
        linuxMain.get().dependsOn(posixMain)
        macosMain.get().dependsOn(posixMain)
    }
}
