package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.TerminalInterface
import com.github.ajalt.mordant.terminal.terminalinterface.TerminalInterfaceNativeCopyPasted

internal actual fun getStandardTerminalInterface(): TerminalInterface {
    return TerminalInterfaceNativeCopyPasted()
}
