package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.*


// Since `js()` and `external` work differently in wasm and js, we need to define the functions that
// use them twice
internal expect fun makeNodeMppImpls(): JsMppImpls?
internal expect fun browserPrintln(message: String)

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

private class JsAtomicInt(initial: Int) : MppAtomicInt {
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


internal interface JsMppImpls {
    fun readEnvvar(key: String): String?
    fun stdoutInteractive(): Boolean
    fun stdinInteractive(): Boolean
    fun stderrInteractive(): Boolean
    fun getTerminalSize(): Size?
    fun printStderr(message: String, newline: Boolean)
    fun readLineOrNull(): String?
    fun makeTerminalCursor(terminal: Terminal): TerminalCursor
    fun exitProcess(status: Int)
    fun cwd(): String
    fun readFileIfExists(filename: String): String?
}

private object BrowserMppImpls : JsMppImpls {
    override fun readEnvvar(key: String): String? = null
    override fun stdoutInteractive(): Boolean = false
    override fun stdinInteractive(): Boolean = false
    override fun stderrInteractive(): Boolean = false
    override fun getTerminalSize(): Size? = null
    override fun printStderr(message: String, newline: Boolean) = browserPrintln(message)
    override fun exitProcess(status: Int) {}
    override fun cwd(): String {
        return "??"
    }

    // readlnOrNull will just throw an exception on browsers
    override fun readLineOrNull(): String? = readlnOrNull()
    override fun makeTerminalCursor(terminal: Terminal): TerminalCursor {
        return BrowserTerminalCursor(terminal)
    }

    override fun readFileIfExists(filename: String): String? = null
}

internal abstract class BaseNodeMppImpls<BufferT> : JsMppImpls {
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

private val impls: JsMppImpls = makeNodeMppImpls() ?: BrowserMppImpls

internal actual fun runningInIdeaJavaAgent(): Boolean = false

internal actual fun getTerminalSize(): Size? = impls.getTerminalSize()
internal actual fun getEnv(key: String): String? = impls.readEnvvar(key)
internal actual fun stdoutInteractive(): Boolean = impls.stdoutInteractive()
internal actual fun stdinInteractive(): Boolean = impls.stdinInteractive()
internal actual fun printStderr(message: String, newline: Boolean) {
    impls.printStderr(message, newline)
}

internal actual fun exitProcessMpp(status: Int): Unit = impls.exitProcess(status)

// hideInput is not currently implemented
internal actual fun readLineOrNullMpp(hideInput: Boolean): String? = impls.readLineOrNull()


internal actual fun makePrintingTerminalCursor(terminal: Terminal): TerminalCursor {
    return impls.makeTerminalCursor(terminal)
}

internal actual fun readFileIfExists(filename: String): String? = impls.readFileIfExists(filename)

// There are no shutdown hooks on browsers, so we don't need to do anything here
private class BrowserTerminalCursor(terminal: Terminal) : PrintTerminalCursor(terminal)


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
internal actual fun runningInBrowser(): Boolean = impls is BrowserMppImpls
internal actual fun cwd(): String {
    return impls.cwd()
}
