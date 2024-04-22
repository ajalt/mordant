package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.PrintTerminalCursor
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalCursor

private external interface Stream {
    val isTTY: Boolean
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
}

private external object fs {
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

private class NodeMppImpls : JsMppImpls {
    override fun readEnvvar(key: String): String? = nodeReadEnvvar(key)
    override fun stdoutInteractive(): Boolean = process.stdout.isTTY
    override fun stdinInteractive(): Boolean = process.stdin.isTTY
    override fun stderrInteractive(): Boolean = process.stderr.isTTY
    override fun getTerminalSize(): Size? {
        if (!nodeWidowSizeIsDefined()) return null
        val jsSize = process.stdout.getWindowSize()
        return Size(width = jsSize[0]!!.toInt(), height = jsSize[1]!!.toInt())
    }

    override fun printStderr(message: String, newline: Boolean) {
        process.stderr.write(if (newline) message + "\n" else message)
    }

    override fun readLineOrNull(): String? {
        return try {
            buildString {
                var char: String
                val buf = Buffer.alloc(1)
                do {
                    fs.readSync(fd = 0, buffer = buf, offset = 0, len = 1, position = null)
                    char = "$buf" // don't call toString here due to KT-55817
                    append(char)
                } while (char != "\n")
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun makeTerminalCursor(terminal: Terminal): TerminalCursor {
        return NodeTerminalCursor(terminal)
    }
}

internal actual fun browserPrintln(message: String): Unit = js("console.error(message)")

internal actual fun makeNodeMppImpls(): JsMppImpls? {
    return if (runningOnNode() && nodeFsModuleAvailable()) NodeMppImpls() else null
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


private external interface CodePointString {
    fun codePointAt(index: Int): Int
}

private external interface StringIteration {
    val value: CodePointString?
}

private external interface StringIterator {
    fun next(): StringIteration
}

private fun stringIterator(@Suppress("UNUSED_PARAMETER") s: String): StringIterator =
    js("s[Symbol.iterator]()")

internal actual fun codepointSequence(string: String): Sequence<Int> {
    val it = stringIterator(string)
    return generateSequence { it.next().value?.codePointAt(0) }
}

// See jsMain/MppImpl.kt for the details of node detection
private fun runningOnNode(): Boolean =
    js("Object.prototype.toString.call(typeof process !== 'undefined' ? process : 0) === '[object process]'")

private fun nodeFsModuleAvailable(): Boolean =
    js(
        """{
            try {
                module['' + 'require']("fs")
                return true
            } catch(e) {
                return false
            }
        }"""
    )
