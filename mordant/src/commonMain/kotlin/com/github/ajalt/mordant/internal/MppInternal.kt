package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.terminal.*
import kotlin.time.Duration

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

/** Update the reference via spin lock, spinning up to [attempts] times. */
internal inline fun <T> MppAtomicRef<T>.update(attempts: Int = 99, block: T.() -> T): Pair<T, T> {
    repeat(attempts) {
        val old = value
        val newValue = block(old)
        if (compareAndSet(old, newValue)) return old to newValue
    }
    throw ConcurrentModificationException("Failed to update state due to concurrent updates")
}

internal expect fun <T> MppAtomicRef(value: T): MppAtomicRef<T>

internal expect fun MppAtomicInt(initial: Int): MppAtomicInt

internal expect fun getEnv(key: String): String?

/** Return a pair of [width, height], or null if it can't be detected */
internal expect fun getTerminalSize(): Size?

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

internal expect val FAST_ISATTY: Boolean

internal expect val CR_IMPLIES_LF: Boolean

internal expect fun exitProcessMpp(status: Int)

internal expect fun readFileIfExists(filename: String): String?

internal expect fun hasFileSystem(): Boolean

internal expect fun readKeyMpp(timeout: Duration): KeyboardEvent?

internal expect fun enterRawModeMpp(): AutoCloseable
