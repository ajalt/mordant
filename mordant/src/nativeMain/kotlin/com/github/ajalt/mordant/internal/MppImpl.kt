package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.*
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO
import platform.posix.getenv
import platform.posix.isatty

internal actual class AtomicInt actual constructor(initial: Int) {
    private val backing = kotlin.native.concurrent.AtomicInt(initial)
    actual fun getAndIncrement(): Int {
        return backing.addAndGet(1)
    }
}

internal actual fun terminalSizeDetectionIsFast(): Boolean = true

internal actual fun getJavaProperty(key: String): String? = null

internal actual fun runningInIdeaJavaAgent(): Boolean = false

internal actual fun getEnv(key: String): String? = getenv(key)?.toKStringFromUtf8()

internal actual fun stdoutInteractive(): Boolean = isatty(STDOUT_FILENO) != 0

internal actual fun stdinInteractive(): Boolean = isatty(STDIN_FILENO) != 0

internal actual fun codepointSequence(string: String): Sequence<Int> = sequence {
    var i = 0
    val chars = string.toCharArray()
    while (i < chars.size) {
        if (i < chars.lastIndex && Char.isSurrogatePair(chars[i], chars[i + 1])) {
            yield(Char.toCodePoint(chars[i], chars[i + 1]))
            i += 1
        } else {
            yield(chars[i].toInt())
        }
        i += 1
    }
}

internal actual fun makePrintingTerminalCursor(terminal: Terminal): TerminalCursor = NativeTerminalCursor(terminal)

private class NativeTerminalCursor(terminal: Terminal) : PrintTerminalCursor(terminal) {
    // TODO: implement native showOnExit for cursor show/hide
}


@OptIn(ExperimentalTerminalApi::class)
internal actual fun sendInterceptedPrintRequest(
    request: PrintRequest,
    terminalInterface: TerminalInterface,
    interceptors: List<TerminalInterceptor>,
    lock: Any,
) {
    terminalInterface.completePrintRequest(
        interceptors.fold(request) { acc, it -> it.intercept(acc) }
    )
}
