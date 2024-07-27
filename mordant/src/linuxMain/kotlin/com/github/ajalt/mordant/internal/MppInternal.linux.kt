package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.internal.syscalls.SyscallHandler
import com.github.ajalt.mordant.internal.syscalls.SyscallHandlerNativeLinux

internal actual fun hasFileSystem(): Boolean = true
internal actual fun getSyscallHandler(): SyscallHandler = SyscallHandlerNativeLinux
