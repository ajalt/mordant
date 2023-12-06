package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.*

internal interface MppAtomicInt {
    fun getAndIncrement(): Int
    fun get(): Int
    fun set(value: Int)
}

internal interface MppAtomicRef<T> {
    val value: T
    fun compareAndSet(expected: T, newValue: T): Boolean
    fun getAndSet(newValue: T): T
}

internal inline fun <T> MppAtomicRef<T>.update(attempts: Int = 10, block: T.() -> T): Pair<T, T>? {
    repeat(attempts) { // spin a few times; skip the update if we still can't succeed
        val old = value
        val newValue = block(old)
        if (compareAndSet(old, newValue)) return old to newValue
    }
    return null
}

internal expect fun <T> MppAtomicRef(value: T): MppAtomicRef<T>

internal expect fun MppAtomicInt(initial: Int): MppAtomicInt

internal expect fun getEnv(key: String): String?

/** Returns pair of [width, height], or null if it can't be detected */
internal expect fun getTerminalSize(): Pair<Int, Int>?

internal expect fun runningInIdeaJavaAgent(): Boolean

internal expect fun stdoutInteractive(): Boolean

internal expect fun stdinInteractive(): Boolean

internal expect fun codepointSequence(string: String): Sequence<Int>

internal expect fun makePrintingTerminalCursor(terminal: Terminal): TerminalCursor

internal expect fun printStderr(message: String, newline: Boolean)

internal expect fun readLineOrNullMpp(hideInput: Boolean): String?

internal expect fun sendInterceptedPrintRequest(
    request: PrintRequest,
    terminalInterface: TerminalInterface,
    interceptors: List<TerminalInterceptor>,
)
