package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.terminalinterface.TerminalInterfacePosix
import platform.posix.ECHO

// https://www.gnu.org/software/libc/manual/html_node/getpass.html
internal actual fun ttySetEcho(echo: Boolean) {
    val handlerPosix = STANDARD_TERM_INTERFACE as TerminalInterfacePosix
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
