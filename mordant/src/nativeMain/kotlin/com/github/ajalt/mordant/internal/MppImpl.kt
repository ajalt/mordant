package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.*
import kotlinx.cinterop.*
import platform.posix.*
import kotlin.native.concurrent.AtomicReference


internal actual class AtomicInt actual constructor(initial: Int) {
    private val backing = kotlin.native.concurrent.AtomicInt(initial)
    actual fun getAndIncrement(): Int {
        return backing.addAndGet(1) - 1
    }

    actual fun get(): Int {
        return backing.value
    }

    actual fun set(value: Int) {
        backing.value = value
    }
}

internal actual fun terminalSizeDetectionIsFast(): Boolean = true

internal actual fun getJavaProperty(key: String): String? = null

internal actual fun runningInIdeaJavaAgent(): Boolean = false

internal actual fun getEnv(key: String): String? = getenv(key)?.toKStringFromUtf8()

internal actual fun stdoutInteractive(): Boolean = isatty(STDOUT_FILENO) != 0

internal actual fun stdinInteractive(): Boolean = isatty(STDIN_FILENO) != 0

internal actual fun stderrInteractive(): Boolean = isatty(STDERR_FILENO) != 0

internal actual fun codepointSequence(string: String): Sequence<Int> = sequence {
    var i = 0
    while (i < string.length) {
        if (i < string.lastIndex && Char.isSurrogatePair(string[i], string[i + 1])) {
            yield(Char.toCodePoint(string[i], string[i + 1]))
            i += 1
        } else {
            yield(string[i].code)
        }
        i += 1
    }
}

internal actual fun printStderr(message: String, newline: Boolean) {
    val s = if (newline) message + "\n"  else message
    fprintf(stderr, s)
    fflush(stderr)
}

internal expect fun ttySetEcho(echo: Boolean)

internal actual fun readLineOrNullMpp(hideInput: Boolean): String? {
    if (hideInput) {
        ttySetEcho(false)
    }
    val lineOrNull =  readlnOrNull()
    if (hideInput) {
        ttySetEcho(true)
    }

    return lineOrNull
}

internal actual fun makePrintingTerminalCursor(terminal: Terminal): TerminalCursor = NativeTerminalCursor(terminal)


// These are for the NativeTerminalCursor, but are top-level since atexit and signal require static
// functions.
private val registeredAtExit = kotlin.native.concurrent.AtomicInt(0)
private const val CURSOR_SHOW_STR = "\u001B[?25h"
private val CURSOR_SHOW_BUF = CURSOR_SHOW_STR.cstr // .ctr allocates, so we need to do it statically

private fun cursorAtExitCallback() {
    // We can't unregister atexit callbacks, so this will print the code even if the cursor has been
    // shown manually. We'd like to have another variable to keep track of that case, but due to
    // KT-45565, we can't access any state in an atexit handler.
    println(CURSOR_SHOW_STR)
}

// In case the user already has a sigint handler installed, we need to keep track of it
private val existingSigintHandler = AtomicReference<CPointer<CFunction<(Int) -> Unit>>?>(null)

@OptIn(UnsafeNumber::class) // for `write`
private fun cursorSigintHandler(signum: Int) {
    signal(SIGINT, SIG_IGN) // disable sigint handling to avoid recursive calls
    // signal handlers can't safely access most state or functions due to their async nature, so we
    // have to write to stdout directly without doing any allocation
    write(
        STDOUT_FILENO,
        CURSOR_SHOW_BUF,
        // `CURSOR_SHOW_STR.length == 6`. We use a literal since that parameter is a UInt on mingw and a ULong on posix
        6
    )
    signal(SIGINT, existingSigintHandler.value ?: SIG_DFL) // reset signal handling to previous value
    existingSigintHandler.value = null
    raise(signum) // re-raise the signal
}

private val cursorSigintHandlerPtr = staticCFunction(::cursorSigintHandler)

private class NativeTerminalCursor(terminal: Terminal) : PrintTerminalCursor(terminal) {
    override fun hide(showOnExit: Boolean) {
        if (showOnExit && registeredAtExit.compareAndSet(0, 1)) {
            atexit(staticCFunction(::cursorAtExitCallback))
            val handler = signal(SIGINT, cursorSigintHandlerPtr)
            if (handler != cursorSigintHandlerPtr) {
                existingSigintHandler.value = handler
            }
        }

        super.hide(showOnExit)
    }
}


@OptIn(ExperimentalTerminalApi::class)
internal actual fun sendInterceptedPrintRequest(
    request: PrintRequest,
    terminalInterface: TerminalInterface,
    interceptors: List<TerminalInterceptor>,
) {
    terminalInterface.completePrintRequest(
        interceptors.fold(request) { acc, it -> it.intercept(acc) }
    )
}

internal actual inline fun synchronizeJvm(lock: Any, block: () -> Unit) = block()
