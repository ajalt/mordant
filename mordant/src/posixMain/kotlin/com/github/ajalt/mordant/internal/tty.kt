package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.internal.syscalls.SyscallHandlerNativePosix
import kotlinx.cinterop.UnsafeNumber
import platform.posix.ECHO

// https://www.gnu.org/software/libc/manual/html_node/getpass.html
internal actual fun ttySetEcho(echo: Boolean) {
    val termios = SyscallHandlerNativePosix.getStdinTermios() ?: return
    SyscallHandlerNativePosix.setStdinTermios(
        termios.copy(
            lflag = when {
                echo -> termios.lflag or ECHO.toUInt()
                else -> termios.lflag and ECHO.inv().toUInt()
            }
        )
    )
}
