package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.*

internal expect class AtomicInt(initial: Int) {
    fun getAndIncrement(): Int
    fun get(): Int
    fun set(value: Int)
}

internal expect fun getEnv(key: String): String?

internal expect fun terminalSizeDetectionIsFast(): Boolean

/** Returns pair of [width, height], or null if it can't be detected */
internal expect fun getTerminalSize(timeoutMs: Long): Pair<Int, Int>?

internal expect fun runningInIdeaJavaAgent(): Boolean

internal expect fun stdoutInteractive(): Boolean

internal expect fun stdinInteractive(): Boolean

internal expect fun stderrInteractive(): Boolean

internal expect fun codepointSequence(string: String): Sequence<Int>

internal expect fun makePrintingTerminalCursor(terminal: Terminal): TerminalCursor

internal expect fun printStderr(message: String, newline: Boolean)

internal expect fun readLineOrNullMpp(hideInput: Boolean): String?

@OptIn(ExperimentalTerminalApi::class)
internal expect fun sendInterceptedPrintRequest(
    request: PrintRequest,
    terminalInterface: TerminalInterface,
    interceptors: List<TerminalInterceptor>,
)

internal expect inline fun synchronizeJvm(lock: Any, block: () -> Unit)
