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
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosX64()
    watchosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val posixMain by creating { dependsOn(nativeMain.get()) }
        linuxMain.get().dependsOn(posixMain)
        appleMain.get().dependsOn(posixMain)
    }
}
