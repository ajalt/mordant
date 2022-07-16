package com.github.ajalt.mordant.internal

import kotlinx.cinterop.*
import platform.posix.*

@OptIn(UnsafeNumber::class)
internal actual fun ttySetEcho(echo: Boolean) {
  updateTermios {
    c_lflag = if (echo) {
      c_lflag.or(ECHO.convert())
    } else {
      c_lflag.and(ECHO.inv().convert())
    }
  }
}

private fun updateTermios(block: termios.() -> Unit) {
  memScoped {
    val termios = alloc<termios>()
    check(tcgetattr(0, termios.ptr) == 0) {
      error("tcgetattr() error: $errno")
    }

    block(termios)

    check(tcsetattr(0, TCSADRAIN, termios.ptr) == 0) {
      error("tcsetattr() error: $errno")
    }
  }
}