package com.github.ajalt.mordant.terminal.terminalinterface

import com.github.ajalt.mordant.terminal.TerminalInterface
import com.github.ajalt.mordant.terminal.TerminalInterfaceProvider

class TerminalInterfaceProviderNativeImage : TerminalInterfaceProvider {
    override fun load(): TerminalInterface? {
        val os = System.getProperty("os.name")
        return when {
            os.startsWith("Windows") -> TerminalInterfaceNativeImageWindows()
            os == "Linux" -> TerminalInterfaceNativeImageLinux()
            os == "Mac OS X" -> TerminalInterfaceNativeImageMacos()
            else -> null
        }
    }
}
