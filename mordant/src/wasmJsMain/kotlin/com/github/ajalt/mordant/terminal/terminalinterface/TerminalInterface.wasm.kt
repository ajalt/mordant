package com.github.ajalt.mordant.terminal.terminalinterface

import com.github.ajalt.mordant.input.MouseTracking
import com.github.ajalt.mordant.rendering.Size
import com.github.ajalt.mordant.terminal.PrintTerminalCursor
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalCursor


private external interface Stream {
    val isTTY: Boolean
    fun setRawMode(mode: Boolean)
    fun write(s: String)
    fun getWindowSize(): JsArray<JsNumber>
}

@Suppress("ClassName")
private external object process {
    val stdout: Stream
    val stdin: Stream
    val stderr: Stream

    fun on(event: String, listener: () -> Unit)
    fun removeListener(event: String, listener: () -> Unit)
    fun exit(status: Int)
}

private external interface FsModule {
    fun readSync(fd: Int, buffer: JsAny, offset: Int, len: Int, position: JsAny?): Int
}

private external object Buffer {
    fun alloc(size: Int): JsAny
}

@Suppress("RedundantNullableReturnType") // invalid diagnostic due to KTIJ-28239
private fun nodeReadEnvvar(@Suppress("UNUSED_PARAMETER") key: String): String? =
    js("process.env[key]")

private fun nodeWidowSizeIsDefined(): Boolean =
    js("process.stdout.getWindowSize != undefined")

// Have to use js() instead of extern since kotlin can't catch exceptions from external wasm
// functions
@Suppress("RedundantNullableReturnType", "UNUSED_PARAMETER")
private fun nodeReadFileSync(filename: String): String? =
    js(
        """{
            try {
                return require('fs').readFileSync(filename).toString()
            } catch (e) {
                return null
            }
        }"""
    )


internal class TerminalInterfaceWasm : TerminalInterfaceNode<JsAny>() {
    private val fs: FsModule = importNodeFsModule()

    override fun readEnvvar(key: String): String? = nodeReadEnvvar(key)
    override fun stdoutInteractive(): Boolean = process.stdout.isTTY
    override fun stdinInteractive(): Boolean = process.stdin.isTTY
    override fun exitProcess(status: Int): Unit = process.exit(status)
    override fun getTerminalSize(): Size? {
        if (!nodeWidowSizeIsDefined()) return null
        val jsSize = process.stdout.getWindowSize()
        return Size(width = jsSize[0]!!.toInt(), height = jsSize[1]!!.toInt())
    }

    override fun printStderr(message: String, newline: Boolean) {
        process.stderr.write(if (newline) message + "\n" else message)
    }

    override fun allocBuffer(size: Int): JsAny = Buffer.alloc(size)

    override fun bufferToString(buffer: JsAny): String = js("buffer.toString()")

    override fun readSync(fd: Int, buffer: JsAny, offset: Int, len: Int): Int {
        return fs.readSync(fd, buffer, offset, len, null)
    }

    override fun makeTerminalCursor(terminal: Terminal): TerminalCursor {
        return NodeTerminalCursor(terminal)
    }

    override fun readFileIfExists(filename: String): String? {
        return nodeReadFileSync(filename)
    }

    override fun enterRawMode(mouseTracking: MouseTracking): AutoCloseable {
        if (!stdinInteractive()) {
            throw RuntimeException("Cannot enter raw mode on a non-interactive terminal")
        }
        process.stdin.setRawMode(true)
        return AutoCloseable { process.stdin.setRawMode(false) }
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
            val function = { show() }
            shutdownHook = function
            process.on("exit", function)
        }
        super.hide(showOnExit)
    }
}

private fun importNodeFsModule(): FsModule = js("""require("fs")""")
