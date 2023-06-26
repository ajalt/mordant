package com.github.ajalt.mordant.internal

import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.*

// https://www.gnu.org/software/libc/manual/html_node/getpass.html
internal actual fun ttySetEcho(echo: Boolean) = memScoped {
    val termios = alloc<termios>()
    if (tcgetattr(STDOUT_FILENO, termios.ptr) != 0) {
        return@memScoped
    }

    termios.c_lflag = if (echo) {
        termios.c_lflag or ECHO.convert()
    } else {
        termios.c_lflag and ECHO.inv().convert()
    }

    tcsetattr(0, TCSAFLUSH, termios.ptr)
}
