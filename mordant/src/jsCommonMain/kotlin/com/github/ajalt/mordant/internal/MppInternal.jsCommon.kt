package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.*
import com.github.ajalt.mordant.terminal.terminalinterface.TerminalInterfaceBrowser
import com.github.ajalt.mordant.terminal.terminalinterface.TerminalInterfaceJsCommon


// Since `js()` and `external` work differently in wasm and js, we need to define the functions that
// use them twice
internal expect fun makeNodeTerminalInterface(): TerminalInterfaceJsCommon?
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


internal actual fun getStandardTerminalInterface(): TerminalInterface {
    return makeNodeTerminalInterface() ?: TerminalInterfaceBrowser
}

private val impls get() = STANDARD_TERM_INTERFACE as TerminalInterfaceJsCommon

internal actual fun runningInIdeaJavaAgent(): Boolean = false

internal actual fun getEnv(key: String): String? = impls.readEnvvar(key)
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


internal actual fun sendInterceptedPrintRequest(
    request: PrintRequest,
    terminalInterface: TerminalInterface,
    interceptors: List<TerminalInterceptor>,
) {
    terminalInterface.completePrintRequest(
        interceptors.fold(request) { acc, it -> it.intercept(acc) }
    )
}

internal actual fun hasFileSystem(): Boolean = impls !is TerminalInterfaceBrowser
