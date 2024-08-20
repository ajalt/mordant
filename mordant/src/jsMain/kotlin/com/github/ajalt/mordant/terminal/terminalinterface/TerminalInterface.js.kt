package com.github.ajalt.mordant.terminal.terminalinterface

import com.github.ajalt.mordant.input.MouseTracking
import com.github.ajalt.mordant.rendering.Size
import com.github.ajalt.mordant.terminal.PrintTerminalCursor
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalCursor


private external val process: dynamic
private external val Buffer: dynamic

internal class TerminalInterfaceJsNode(private val fs: dynamic): TerminalInterfaceNode<dynamic>() {
    override fun readEnvvar(key: String): String? = process.env[key] as? String
    override fun stdoutInteractive(): Boolean = js("Boolean(process.stdout.isTTY)") as Boolean
    override fun stdinInteractive(): Boolean = js("Boolean(process.stdin.isTTY)") as Boolean
    override fun exitProcess(status: Int) {
        process.exit(status)
    }

    override fun getTerminalSize(): Size? {
        // For some undocumented reason, getWindowSize is undefined sometimes, presumably when isTTY
        // is false
        if (process.stdout.getWindowSize == undefined) return null
        val s = process.stdout.getWindowSize()
        return Size(width = s[0] as Int, height = s[1] as Int)
    }

    override fun printStderr(message: String, newline: Boolean) {
        process.stderr.write(if (newline) message + "\n" else message)
    }

    override fun allocBuffer(size: Int): dynamic {
        return Buffer.alloc(size)
    }

    override fun bufferToString(buffer: dynamic): String {
        return js("buffer.toString()") as String
    }

    override fun readSync(fd: Int, buffer: dynamic, offset: Int, len: Int): Int {
        return fs.readSync(fd, buffer, offset, len, null) as Int
    }

    override fun makeTerminalCursor(terminal: Terminal): TerminalCursor {
        return NodeTerminalCursor(terminal)
    }

    override fun readFileIfExists(filename: String): String? {
        return fs.readFileSync(filename, "utf-8") as? String
    }

    override fun enterRawMode(mouseTracking: MouseTracking): AutoCloseable {
        if (!stdinInteractive()) {
            throw RuntimeException("Cannot enter raw mode on a non-interactive terminal")
        }
        process.stdin.setRawMode(true)
        return AutoCloseable {
            process.stdin.setRawMode(false)
            Unit
        }
    }
}

private class NodeTerminalCursor(terminal: Terminal) : PrintTerminalCursor(terminal) {
    private var shutdownHook: (() -> Unit)? = null

    override fun show() {
        shutdownHook?.let { process.removeListener("exit", it) }
        super.show()
    }

    override fun hide(showOnExit: Boolean) {
        if (showOnExit && shutdownHook == null) {
            shutdownHook = { show() }
            process.on("exit", shutdownHook)
        }
        super.hide(showOnExit)
    }
}

