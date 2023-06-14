package com.github.ajalt.mordant.internal

import com.sun.jna.*
import java.io.IOException
import java.util.concurrent.TimeUnit

@Suppress("ClassName", "PropertyName", "MemberVisibilityCanBePrivate", "SpellCheckingInspection")
interface MacosLibC : Library {

    @Suppress("unused")
    class winsize : Structure() {
        @JvmField
        var ws_row: Short = 0

        @JvmField
        var ws_col: Short = 0

        @JvmField
        var ws_xpixel: Short = 0

        @JvmField
        var ws_ypixel: Short = 0

        override fun getFieldOrder(): List<String> {
            return mutableListOf("ws_row", "ws_col", "ws_xpixel", "ws_ypixel")
        }
    }

    fun isatty(fd: Int): Int
    fun ioctl(fd: Int, cmd: NativeLong?, data: winsize?): Int
}

internal class MacosMppImpls : JnaMppImpls {
    @Suppress("SpellCheckingInspection")
    private companion object {
        const val STDIN_FILENO = 0
        const val STDOUT_FILENO = 1
        const val STDERR_FILENO = 2

        val TIOCGWINSZ = when {
            Platform.isMIPS() || Platform.isPPC() || Platform.isSPARC() -> 0x40087468L
            else -> 0x00005413L
        }
    }

    private val libC: MacosLibC = Native.load(Platform.C_LIBRARY_NAME, MacosLibC::class.java)
    override fun stdoutInteractive(): Boolean = libC.isatty(STDOUT_FILENO) == 1
    override fun stdinInteractive(): Boolean = libC.isatty(STDIN_FILENO) == 1
    override fun stderrInteractive(): Boolean = libC.isatty(STDERR_FILENO) == 1

    override fun getTerminalSize(): Pair<Int, Int>? {
        // TODO: this seems to fail on macosArm64, use stty on mac for now
//        val size = MacosLibC.winsize()
//        return if (libC.ioctl(STDIN_FILENO, NativeLong(TIOCGWINSZ), size) < 0) {
//            null
//        } else {
//            size.ws_col.toInt() to size.ws_row.toInt()
//        }
        return getSttySize(100)
    }



}

@Suppress("SameParameterValue")
private fun getSttySize(timeoutMs: Long): Pair<Int, Int>? {
    val process = when {
        // Try running stty both directly and via env, since neither one works on all systems
        else -> runCommand("stty", "size") ?: runCommand("/usr/bin/env", "stty", "size")
    } ?: return null
    try {
        if (!process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)) {
            return null
        }
    } catch (e: InterruptedException) {
        return null
    }

    val output = process.inputStream.bufferedReader().readText().trim()
    return when {
        else -> parseSttySize(output)
    }
}

private fun runCommand(vararg args: String): Process? {
    return try {
        ProcessBuilder(*args)
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .start()
    } catch (e: IOException) {
        null
    }
}

private fun parseSttySize(output: String): Pair<Int, Int>? {
    val dimens = output.split(" ").mapNotNull { it.toIntOrNull() }
    if (dimens.size != 2) return null
    return dimens[1] to dimens[0]
}
