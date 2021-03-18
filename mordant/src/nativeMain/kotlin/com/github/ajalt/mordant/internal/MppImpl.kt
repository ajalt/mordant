package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.*
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO
import platform.posix.getenv
import platform.posix.isatty
import kotlin.native.concurrent.AtomicInt
import platform.posix.atexit
import kotlinx.cinterop.staticCFunction

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

// These are for the NativeTerminalCursor, but are top-level since atexit requires a static function
// This does mean that the callbacks state is shared between all cursor instances. Given that we
// only want to print the show code once anyway, this should be acceptable.
private val registeredAtExit = kotlin.native.concurrent.AtomicInt(0)
private val shouldShow = kotlin.native.concurrent.AtomicInt(0)
private fun cursorAtExitCallback() {
    if (shouldShow.compareAndSet(1, 0)) {
        // An alternative would be to keep a global reference to the terminal that registered the
        // callback, but that would force us to freeze the terminal, which would cause other
        // problems (e.g. registering an interceptor).
        println("$CSI?25h")
    }
}

private class NativeTerminalCursor(terminal: Terminal) : PrintTerminalCursor(terminal) {
    override fun show() {
        shouldShow.value = 0
        super.show()
    }

    override fun hide(showOnExit: Boolean) {
        if (showOnExit && registeredAtExit.compareAndSet(0, 1)) {
            atexit(staticCFunction(::cursorAtExitCallback))
        }

        super.hide(showOnExit)
    }
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
