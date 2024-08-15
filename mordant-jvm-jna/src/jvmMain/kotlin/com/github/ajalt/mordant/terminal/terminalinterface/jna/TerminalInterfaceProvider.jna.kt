package com.github.ajalt.mordant.terminal.terminalinterface.jna

import com.github.ajalt.mordant.terminal.TerminalInterface
import com.github.ajalt.mordant.terminal.TerminalInterfaceProvider
import com.oracle.svm.core.annotate.Substitute
import com.oracle.svm.core.annotate.TargetClass

class TerminalInterfaceProviderJna : TerminalInterfaceProvider {
    override fun load(): TerminalInterface? {
        // Inlined version of ImageInfo.inImageCode()
        val imageCode = System.getProperty("org.graalvm.nativeimage.imagecode")
        val isNativeImage = imageCode == "buildtime" || imageCode == "runtime"
        if (isNativeImage) return null

        val os = System.getProperty("os.name")
        return try {
            when {
                os.startsWith("Windows") -> TerminalInterfaceJnaWindows()
                os == "Linux" -> TerminalInterfaceJnaLinux()
                os == "Mac OS X" -> TerminalInterfaceJnaMacos()
                else -> null
            }
        } catch (e: UnsatisfiedLinkError) {
            null
        }
    }
}

@TargetClass(TerminalInterfaceProviderJna::class)
private class TerminalInterfaceProviderJnaNative {

    @Substitute
    fun load(): TerminalInterface? = null

}
