package com.github.ajalt.mordant.internal.syscalls

import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.internal.Size
import com.github.ajalt.mordant.internal.browserPrintln
import com.github.ajalt.mordant.terminal.PrintTerminalCursor
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalCursor
import kotlin.time.Duration

internal interface SyscallHandlerJsCommon: SyscallHandler {
    fun readEnvvar(key: String): String?
    fun printStderr(message: String, newline: Boolean)
    fun readLineOrNull(): String?
    fun makeTerminalCursor(terminal: Terminal): TerminalCursor
    fun exitProcess(status: Int)
    fun readFileIfExists(filename: String): String?

    // The public interface never is in nonJsMain, so these will never be called
    override fun readKeyEvent(timeout: Duration): KeyboardEvent? {
        throw UnsupportedOperationException("Reading keyboard is not supported on this platform")
    }
    override fun enterRawMode(): AutoCloseable {
        throw UnsupportedOperationException("Raw mode is not supported on this platform")
    }
}

internal abstract class SyscallHandlerNode<BufferT> : SyscallHandlerJsCommon {
    final override fun readLineOrNull(): String? {
        return try {
            buildString {
                val buf = allocBuffer(1)
                do {
                    val len = readSync(
                        fd = 0, buffer = buf, offset = 0, len = 1
                    )
                    if (len == 0) break
                    val char = "$buf" // don't call toString here due to KT-55817
                    append(char)
                } while (char != "\n" && char != "${0.toChar()}")
            }
        } catch (e: Exception) {
            null
        }
    }

    abstract fun allocBuffer(size: Int): BufferT
    abstract fun readSync(fd: Int, buffer: BufferT, offset: Int, len: Int): Int
}

internal object SyscallHandlerBrowser : SyscallHandlerJsCommon {
    override fun readEnvvar(key: String): String? = null
    override fun stdoutInteractive(): Boolean = false
    override fun stdinInteractive(): Boolean = false
    override fun stderrInteractive(): Boolean = false
    override fun getTerminalSize(): Size? = null
    override fun printStderr(message: String, newline: Boolean) = browserPrintln(message)
    override fun exitProcess(status: Int) {}

    // readlnOrNull will just throw an exception on browsers
    override fun readLineOrNull(): String? = readlnOrNull()
    override fun makeTerminalCursor(terminal: Terminal): TerminalCursor {
        return BrowserTerminalCursor(terminal)
    }

    override fun readFileIfExists(filename: String): String? = null
}

// There are no shutdown hooks on browsers, so we don't need to do anything here
private class BrowserTerminalCursor(terminal: Terminal) : PrintTerminalCursor(terminal)
