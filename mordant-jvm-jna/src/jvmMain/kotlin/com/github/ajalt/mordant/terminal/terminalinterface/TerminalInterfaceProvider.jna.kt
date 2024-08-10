package com.github.ajalt.mordant.terminal.terminalinterface

import com.github.ajalt.mordant.terminal.TerminalInterface
import com.github.ajalt.mordant.terminal.TerminalInterfaceProvider

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
