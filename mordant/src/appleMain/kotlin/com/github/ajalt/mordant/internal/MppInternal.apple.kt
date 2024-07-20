package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.TerminalInterface
import com.github.ajalt.mordant.internal.syscalls.SyscallHandlerNativePosix

internal actual fun getStandardTerminalInterface(): TerminalInterface = SyscallHandlerNativeApple
