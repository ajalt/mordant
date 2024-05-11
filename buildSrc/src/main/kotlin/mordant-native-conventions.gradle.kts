plugins {
    kotlin("multiplatform")
}

kotlin {
    linuxX64()
    linuxArm64()
    macosX64()
    macosArm64()
    mingwX64()

    iosX64()
    iosArm64()
    iosSimulatorArm64()
// Not all targets are supported by the markdown library
//    tvosX64()
//    tvosArm64()
    tvosSimulatorArm64()
//    watchosArm32()
//    watchosArm64()
//    watchosDeviceArm64()
//    watchosX64()
    watchosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val posixMain by creating { dependsOn(nativeMain.get()) }
        linuxMain.get().dependsOn(posixMain)
        appleMain.get().dependsOn(posixMain)
        val appleNonDesktopMain by creating { dependsOn(appleMain.get()) }
        for (target in listOf(iosMain, tvosMain, watchosMain)) {
            target.get().dependsOn(appleNonDesktopMain)
        }
    }
}
