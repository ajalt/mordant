package com.github.ajalt.mordant.internal

import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Wincon
import com.sun.jna.ptr.IntByReference

internal interface JnaMppImpls {
    fun stdoutInteractive(): Boolean
    fun stdinInteractive(): Boolean
    fun stderrInteractive(): Boolean
    fun getTerminalSize(timeoutMs: Long): Pair<Int, Int>?
}

private class Win32MppImpls : JnaMppImpls {
    private val stdoutHandle = Kernel32.INSTANCE.GetStdHandle(Wincon.STD_OUTPUT_HANDLE)
    private val stdinHandle = Kernel32.INSTANCE.GetStdHandle(Wincon.STD_INPUT_HANDLE)
    private val stderrHandle = Kernel32.INSTANCE.GetStdHandle(Wincon.STD_ERROR_HANDLE)
    override fun stdoutInteractive(): Boolean {
        return Kernel32.INSTANCE.GetConsoleMode(stdoutHandle, IntByReference())
    }

    override fun stdinInteractive(): Boolean {
        return Kernel32.INSTANCE.GetConsoleMode(stdinHandle, IntByReference())
    }

    override fun stderrInteractive(): Boolean {
        return Kernel32.INSTANCE.GetConsoleMode(stderrHandle, IntByReference())
    }

    override fun getTerminalSize(timeoutMs: Long): Pair<Int, Int>? {
        val csbi = Wincon.CONSOLE_SCREEN_BUFFER_INFO()
        if (!Kernel32.INSTANCE.GetConsoleScreenBufferInfo(stdoutHandle, csbi)) {
            return null
        }
        return csbi.srWindow.run { Right - Left + 1 to Bottom - Top + 1 }
    }
}

private class PosixMppImpls : JnaMppImpls {

}

