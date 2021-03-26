package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.*

internal expect class AtomicInt(initial: Int) {
    fun getAndIncrement(): Int
}

internal expect fun getEnv(key: String): String?

internal expect fun getJavaProperty(key: String): String?

internal expect fun terminalSizeDetectionIsFast(): Boolean

/** Returns pair of [width, height], or null if it can't be detected */
internal expect fun getTerminalSize(timeoutMs: Long): Pair<Int, Int>?

internal expect fun isWindows(): Boolean

internal expect fun runningInIdeaJavaAgent(): Boolean

internal expect fun stdoutInteractive(): Boolean

internal expect fun stdinInteractive(): Boolean

internal expect fun codepointSequence(string: String): Sequence<Int>

internal expect fun makePrintingTerminalCursor(terminal: Terminal): TerminalCursor

@OptIn(ExperimentalTerminalApi::class)
internal expect fun sendInterceptedPrintRequest(
    request: PrintRequest,
    terminalInterface: TerminalInterface,
    interceptors: List<TerminalInterceptor>,
    lock: Any,
)
