package com.github.ajalt.mordant.terminal.terminalinterface

import com.github.ajalt.mordant.terminal.TerminalInterface
import com.github.ajalt.mordant.terminal.TerminalInterfaceProvider

class TerminalInterfaceProviderFfm : TerminalInterfaceProvider {
    override fun load(): TerminalInterface? {
        if (!TerminalInterfaceProviderFfm::class.java.module.isNativeAccessEnabled()) {
            return null
        }
        val os = System.getProperty("os.name")
        return when {
            os.startsWith("Windows") -> TerminalInterfaceFfmWindows()
            os == "Linux" -> TerminalInterfaceFfmLinux()
            os == "Mac OS X" -> TerminalInterfaceFfmMacos()
            else -> null
        }
    }
}
