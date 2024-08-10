package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.TerminalInterface
import com.github.ajalt.mordant.terminal.terminalinterface.TerminalInterfaceNativeApple

internal actual fun getStandardTerminalInterface(): TerminalInterface {
    return TerminalInterfaceNativeApple()
}
