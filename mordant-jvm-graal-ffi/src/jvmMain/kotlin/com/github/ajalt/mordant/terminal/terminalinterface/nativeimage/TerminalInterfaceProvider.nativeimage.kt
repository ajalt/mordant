package com.github.ajalt.mordant.terminal.terminalinterface.nativeimage

import com.github.ajalt.mordant.terminal.TerminalInterface
import com.github.ajalt.mordant.terminal.TerminalInterfaceProvider
import com.oracle.svm.core.annotate.Substitute
import com.oracle.svm.core.annotate.TargetClass
import org.graalvm.nativeimage.Platform
import org.graalvm.nativeimage.Platforms

class TerminalInterfaceProviderNativeImage : TerminalInterfaceProvider {
    override fun load(): TerminalInterface? = null
}

@Platforms(Platform.LINUX::class)
@TargetClass(TerminalInterfaceProviderNativeImage::class)
private class TerminalInterfaceProviderNativeImageLinux {

    @Substitute
    fun load(): TerminalInterface = TerminalInterfaceNativeImageLinux()

}

@Platforms(Platform.WINDOWS::class)
@TargetClass(TerminalInterfaceProviderNativeImage::class)
private class TerminalInterfaceProviderNativeImageWindows {

    @Substitute
    fun load(): TerminalInterface = TerminalInterfaceNativeImageWindows()

}

@Platforms(Platform.MACOS::class)
@TargetClass(TerminalInterfaceProviderNativeImage::class)
private class TerminalInterfaceProviderNativeImageMacos {

    @Substitute
    fun load(): TerminalInterface = TerminalInterfaceNativeImageMacos()

}
