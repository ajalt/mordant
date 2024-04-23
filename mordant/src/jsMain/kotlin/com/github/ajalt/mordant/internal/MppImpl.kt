package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.PrintTerminalCursor
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalCursor

private external val process: dynamic
private external val console: dynamic
private external val Symbol: dynamic
private external val Buffer: dynamic

internal actual fun browserPrintln(message: String) {
    // No way to avoid the newline on browsers
    console.error(message)
}

private class NodeMppImpls(private val fs: dynamic) : BaseNodeMppImpls<dynamic>() {
    override fun readEnvvar(key: String): String? = process.env[key] as? String
    override fun stdoutInteractive(): Boolean = js("Boolean(process.stdout.isTTY)") as Boolean
    override fun stdinInteractive(): Boolean = js("Boolean(process.stdin.isTTY)") as Boolean
    override fun stderrInteractive(): Boolean = js("Boolean(process.stderr.isTTY)") as Boolean
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

    override fun readSync(fd: Int, buffer: dynamic, offset: Int, len: Int): Int {
        return fs.readSync(fd, buffer, offset, len, null) as Int
    }

    override fun makeTerminalCursor(terminal: Terminal): TerminalCursor {
        return NodeTerminalCursor(terminal)
    }
}

internal actual fun makeNodeMppImpls(): JsMppImpls? {
    return try {
        NodeMppImpls(nodeRequire("fs"))
    } catch (e: Exception) {
        null
    }
}

internal actual fun codepointSequence(string: String): Sequence<Int> {
    val it = string.asDynamic()[Symbol.iterator]()
    return generateSequence {
        it.next()["value"]?.codePointAt(0) as? Int
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
