package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.internal.syscalls.SyscallHandler
import com.github.ajalt.mordant.internal.syscalls.SyscallHandlerNativeWindows

internal actual fun ttySetEcho(echo: Boolean) = SyscallHandlerNativeWindows.ttySetEcho(echo)
internal actual fun hasFileSystem(): Boolean = true
internal actual fun getSyscallHandler(): SyscallHandler = SyscallHandlerNativeWindows
