package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.internal.syscalls.SyscallHandler
import com.github.ajalt.mordant.internal.syscalls.SyscallHandlerNativeApple

internal actual fun getSyscallHandler(): SyscallHandler = SyscallHandlerNativeApple
