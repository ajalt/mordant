package com.github.ajalt.mordant.internal

import com.sun.jna.*
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions

// Interface definitions from
// https://github.com/java-native-access/jna/blob/master/contrib/platform/src/com/sun/jna/platform/win32/Kernel32.java
// copied here so that we don't need the entire platform dependency
@Suppress("FunctionName", "PropertyName", "ClassName", "unused")
private interface WinKernel32Lib : Library {
    companion object {
        const val STD_INPUT_HANDLE = -10
        const val STD_OUTPUT_HANDLE = -11
        const val STD_ERROR_HANDLE = -12
    }
    class HANDLE : PointerType()

    @Structure.FieldOrder("X", "Y")
    class COORD : Structure() {
        @JvmField
        var X: Short = 0

        @JvmField
        var Y: Short = 0
    }

    @Structure.FieldOrder("Left", "Top", "Right", "Bottom")
    class SMALL_RECT : Structure() {
        @JvmField
        var Left: Short = 0

        @JvmField
        var Top: Short = 0

        @JvmField
        var Right: Short = 0

        @JvmField
        var Bottom: Short = 0
    }

    @Structure.FieldOrder("dwSize", "dwCursorPosition", "wAttributes", "srWindow", "dwMaximumWindowSize")
    class CONSOLE_SCREEN_BUFFER_INFO : Structure() {
        @JvmField
        var dwSize: COORD? = null

        @JvmField
        var dwCursorPosition: COORD? = null

        @JvmField
        var wAttributes: Short = 0

        @JvmField
        var srWindow: SMALL_RECT? = null

        @JvmField
        var dwMaximumWindowSize: COORD? = null
    }

    fun GetStdHandle(nStdHandle: Int): HANDLE
    fun GetConsoleMode(hConsoleHandle: HANDLE, lpMode: IntByReference): Boolean
    fun GetConsoleScreenBufferInfo(
        hConsoleOutput: HANDLE,
        lpConsoleScreenBufferInfo: CONSOLE_SCREEN_BUFFER_INFO,
    ): Boolean
}

internal class Win32MppImpls : JnaMppImpls {
    private val kernel = Native.load("kernel32", WinKernel32Lib::class.java, W32APIOptions.DEFAULT_OPTIONS);
    private val stdoutHandle = kernel.GetStdHandle(WinKernel32Lib.STD_OUTPUT_HANDLE)
    private val stdinHandle = kernel.GetStdHandle(WinKernel32Lib.STD_INPUT_HANDLE)
    private val stderrHandle = kernel.GetStdHandle(WinKernel32Lib.STD_ERROR_HANDLE)
    override fun stdoutInteractive(): Boolean {
        return kernel.GetConsoleMode(stdoutHandle, IntByReference())
    }

    override fun stdinInteractive(): Boolean {
        return kernel.GetConsoleMode(stdinHandle, IntByReference())
    }

    override fun stderrInteractive(): Boolean {
        return kernel.GetConsoleMode(stderrHandle, IntByReference())
    }

    override fun getTerminalSize(): Pair<Int, Int>? {
        val csbi = WinKernel32Lib.CONSOLE_SCREEN_BUFFER_INFO()
        if (!kernel.GetConsoleScreenBufferInfo(stdoutHandle, csbi)) {
            return null
        }
        return csbi.srWindow?.run { Right - Left + 1 to Bottom - Top + 1 }
    }
}
