package com.github.ajalt.mordant.terminal.terminalinterface

import com.github.ajalt.mordant.input.InputEvent
import com.github.ajalt.mordant.input.MouseTracking
import com.github.ajalt.mordant.internal.browserPrintln
import com.github.ajalt.mordant.rendering.Size
import com.github.ajalt.mordant.terminal.PrintTerminalCursor
import com.github.ajalt.mordant.terminal.StandardTerminalInterface
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalCursor
import kotlin.time.TimeMark

internal abstract class TerminalInterfaceJsCommon : StandardTerminalInterface() {
    abstract fun readEnvvar(key: String): String?
    abstract fun printStderr(message: String, newline: Boolean)
    abstract fun readLineOrNull(): String?
    abstract fun makeTerminalCursor(terminal: Terminal): TerminalCursor
    abstract fun exitProcess(status: Int)
    abstract fun readFileIfExists(filename: String): String?

}

internal abstract class TerminalInterfaceNode<BufferT> : TerminalInterfaceJsCommon() {
    final override fun readLineOrNull(): String? {
        return try {
            buildString {
                val buf = allocBuffer(1)
                do {
                    val char = readByteWithBuf(buf) ?: break
                    append(char)
                } while (char != "\n" && char != "${0.toChar()}")
            }
        } catch (e: Exception) {
            null
        }
    }

    abstract fun allocBuffer(size: Int): BufferT
    abstract fun bufferToString(buffer: BufferT): String
    abstract fun readSync(fd: Int, buffer: BufferT, offset: Int, len: Int): Int

    override fun readInputEvent(timeout: TimeMark, mouseTracking: MouseTracking): InputEvent? {
        return PosixEventParser {
            readByteWithBuf(allocBuffer(1))?.let { it[0].code }
        }.readInputEvent(timeout)
    }

    private fun readByteWithBuf(buf: BufferT): String? {
        val len = readSync(fd = 0, buffer = buf, offset = 0, len = 1)
        if (len == 0) return null
        // don't call kotlin's toString here due to KT-55817
        return bufferToString(buf)
    }
}

internal object TerminalInterfaceBrowser : TerminalInterfaceJsCommon() {
    override fun readEnvvar(key: String): String? = null
    override fun stdoutInteractive(): Boolean = false
    override fun stdinInteractive(): Boolean = false
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
