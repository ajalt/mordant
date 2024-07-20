package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.TerminalInterface
import com.github.ajalt.mordant.terminal.`interface`.TerminalInterfaceNativeWindows

internal actual fun ttySetEcho(echo: Boolean) = TerminalInterfaceNativeWindows.ttySetEcho(echo)
internal actual fun hasFileSystem(): Boolean = true
internal actual fun getStandardTerminalInterface(): TerminalInterface = TerminalInterfaceNativeWindows
