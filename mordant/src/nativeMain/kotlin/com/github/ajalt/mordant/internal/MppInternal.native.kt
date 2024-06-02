package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.*
import kotlinx.cinterop.*
import platform.posix.*
import kotlin.concurrent.AtomicInt
import kotlin.concurrent.AtomicReference
import kotlin.system.exitProcess

private class NativeAtomicRef<T>(value: T) : MppAtomicRef<T> {
    private val ref = AtomicReference(value)
    override val value: T
        get() = ref.value

    override fun compareAndSet(expected: T, newValue: T): Boolean {
        return ref.compareAndSet(expected, newValue)
    }

    override fun getAndSet(newValue: T): T {
        return ref.getAndSet(newValue)
    }
}

private class NativeAtomicInt(initial: Int) : MppAtomicInt {
    private val backing = AtomicInt(initial)
    override fun getAndIncrement(): Int {
        return backing.addAndGet(1) - 1
    }

    override fun get(): Int {
        return backing.value
    }

    override fun set(value: Int) {
        backing.value = value
    }
}

internal actual fun MppAtomicInt(initial: Int): MppAtomicInt = NativeAtomicInt(initial)
internal actual fun <T> MppAtomicRef(value: T): MppAtomicRef<T> = NativeAtomicRef(value)

internal actual fun runningInIdeaJavaAgent(): Boolean = false

internal actual fun getEnv(key: String): String? = getenv(key)?.toKStringFromUtf8()

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
    val s = if (newline) message + "\n" else message
    fprintf(stderr, s)
    fflush(stderr)
}

// TODO: use the syscall handler for this?
internal expect fun ttySetEcho(echo: Boolean)

internal actual fun readLineOrNullMpp(hideInput: Boolean): String? {
    if (hideInput) ttySetEcho(false)
    try {
        return readlnOrNull()
    } finally {
        if (hideInput) ttySetEcho(true)
    }
}

internal actual fun makePrintingTerminalCursor(terminal: Terminal): TerminalCursor =
    NativeTerminalCursor(terminal)


// These are for the NativeTerminalCursor, but are top-level since atexit and signal require static
// functions.
private val registeredAtExit = AtomicInt(0)
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
        6u
    )
    signal(
        SIGINT,
        existingSigintHandler.value ?: SIG_DFL
    ) // reset signal handling to previous value
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

private val printRequestLock = AtomicInt(0)

internal actual fun sendInterceptedPrintRequest(
    request: PrintRequest,
    terminalInterface: TerminalInterface,
    interceptors: List<TerminalInterceptor>,
) {
    while (printRequestLock.compareAndSet(0, 1)) {
        // spin until we get the lock
    }
    try {
        terminalInterface.completePrintRequest(
            interceptors.fold(request) { acc, it -> it.intercept(acc) }
        )
    } finally {
        printRequestLock.value = 0
    }
}

internal actual fun readFileIfExists(filename: String): String? {
    val file = fopen(filename, "r") ?: return null
    val chunks = StringBuilder()
    try {
        memScoped {
            val bufferLength = 64 * 1024
            val buffer = allocArray<ByteVar>(bufferLength)

            while (true) {
                val chunk = fgets(buffer, bufferLength, file)?.toKString()
                if (chunk.isNullOrEmpty()) break
                chunks.append(chunk)
            }
        }
    } finally {
        fclose(file)
    }
    return chunks.toString()
}

internal actual fun exitProcessMpp(status: Int): Unit = exitProcess(status)
internal actual val CR_IMPLIES_LF: Boolean = false
