package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.TerminalInterface
import com.github.ajalt.mordant.terminal.terminalinterface.TerminalInterfaceNativeLinux

internal actual fun hasFileSystem(): Boolean = true
internal actual fun getStandardTerminalInterface(): TerminalInterface = TerminalInterfaceNativeLinux
