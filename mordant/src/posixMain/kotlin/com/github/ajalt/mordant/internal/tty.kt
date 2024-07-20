package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.internal.syscalls.SyscallHandlerNativePosix
import platform.posix.ECHO

// https://www.gnu.org/software/libc/manual/html_node/getpass.html
internal actual fun ttySetEcho(echo: Boolean) {
    val handlerPosix = SYSCALL_HANDLER as SyscallHandlerPosix
    val termios = handlerPosix.getStdinTermios()
    handlerPosix.setStdinTermios(
        termios.copy(
            lflag = when {
                echo -> termios.lflag or ECHO.toUInt()
                else -> termios.lflag and ECHO.inv().toUInt()
            }
        )
    )
}
