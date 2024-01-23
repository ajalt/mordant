package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.*

private external val process: dynamic
private external val console: dynamic
private external val Symbol: dynamic
private external val Buffer: dynamic

private class JsAtomicRef<T>(override var value: T) : MppAtomicRef<T> {
    override fun compareAndSet(expected: T, newValue: T): Boolean {
        if (value != expected) return false
        value = newValue
        return true
    }

    override fun getAndSet(newValue: T): T {
        val old = value
        value = newValue
        return old
    }
}

private class JsAtomicInt(initial: Int) : MppAtomicInt{
    private var backing = initial
    override fun getAndIncrement(): Int {
        return backing++
    }

    override fun get(): Int {
        return backing
    }

    override fun set(value: Int) {
        backing = value
    }
}

internal actual fun MppAtomicInt(initial: Int): MppAtomicInt = JsAtomicInt(initial)
internal actual fun <T> MppAtomicRef(value: T): MppAtomicRef<T> = JsAtomicRef(value)


private interface JsMppImpls {
    fun readEnvvar(key: String): String?
    fun stdoutInteractive(): Boolean
    fun stdinInteractive(): Boolean
    fun stderrInteractive(): Boolean
    fun getTerminalSize(): Size?
    fun printStderr(message: String, newline: Boolean)
    fun readLineOrNull(): String?
}

private object BrowserMppImpls : JsMppImpls {
    override fun readEnvvar(key: String): String? = null
    override fun stdoutInteractive(): Boolean = false
    override fun stdinInteractive(): Boolean = false
    override fun stderrInteractive(): Boolean = false
    override fun getTerminalSize(): Size? = null
    override fun printStderr(message: String, newline: Boolean) {
        // No way to avoid the newline on browsers
        console.error(message)
    }

    // readlnOrNull will just throw an exception on browsers
    override fun readLineOrNull(): String? = readlnOrNull()
}

private class NodeMppImpls(private val fs: dynamic) : JsMppImpls {
    override fun readEnvvar(key: String): String? = process.env[key] as? String
    override fun stdoutInteractive(): Boolean = js("Boolean(process.stdout.isTTY)") as Boolean
    override fun stdinInteractive(): Boolean = js("Boolean(process.stdin.isTTY)") as Boolean
    override fun stderrInteractive(): Boolean = js("Boolean(process.stderr.isTTY)") as Boolean
    override fun getTerminalSize(): Size? {
        // For some undocumented reason, getWindowSize is undefined sometimes, presumably when isTTY
        // is false
        if (process.stdout.getWindowSize == undefined) return null
        val s = process.stdout.getWindowSize()
        return Size(width = s[0] as Int, height =  s[1] as Int)
    }

    override fun printStderr(message: String, newline: Boolean) {
        val s = if (newline) message + "\n" else message
        process.stderr.write(s)
    }

    override fun readLineOrNull(): String? {
        return try {
            buildString {
                var char: String
                val buf = Buffer.alloc(1)
                do {
                    fs.readSync(fd = 0, bufer = buf, offset = 0, len = 1, position = null)
                    char = "$buf" // don't call toString here due to KT-55817
                    append(char)
                } while (char != "\n")
            }
        } catch (e: Exception) {
            null
        }
    }
}

private val impls: JsMppImpls = try {
    NodeMppImpls(nodeRequire("fs"))
} catch (e: Exception) {
    BrowserMppImpls
}

internal actual fun runningInIdeaJavaAgent(): Boolean = false

internal actual fun getTerminalSize(): Size? = impls.getTerminalSize()
internal actual fun getEnv(key: String): String? = impls.readEnvvar(key)
internal actual fun stdoutInteractive(): Boolean = impls.stdoutInteractive()
internal actual fun stdinInteractive(): Boolean = impls.stdinInteractive()
internal actual fun printStderr(message: String, newline: Boolean) = impls.printStderr(message, newline)

// hideInput is not currently implemented
internal actual fun readLineOrNullMpp(hideInput: Boolean): String? = impls.readLineOrNull()


internal actual fun codepointSequence(string: String): Sequence<Int> {
    val it = string.asDynamic()[Symbol.iterator]()
    return generateSequence {
        it.next()["value"]?.codePointAt(0) as? Int
    }
}

internal actual fun makePrintingTerminalCursor(terminal: Terminal): TerminalCursor = JsTerminalCursor(terminal)

private class JsTerminalCursor(terminal: Terminal) : PrintTerminalCursor(terminal) {
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


internal actual fun sendInterceptedPrintRequest(
    request: PrintRequest,
    terminalInterface: TerminalInterface,
    interceptors: List<TerminalInterceptor>,
) {
    terminalInterface.completePrintRequest(
        interceptors.fold(request) { acc, it -> it.intercept(acc) }
    )
}

internal actual val FAST_ISATTY: Boolean = true
