package com.github.ajalt.mordant

import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.*


internal actual fun getProperty(key: String): String? =
        error("Not implemented")

internal actual fun getEnv(key: String): String? = getenv(key)?.toKStringFromUtf8()

internal actual fun stdoutInteractive(): Boolean =
        isatty(STDOUT_FILENO) != 0 || isIntellijConsole()

internal actual fun stdinInteractive(): Boolean =
        isatty(STDIN_FILENO) != 0 || isIntellijConsole()

// TODO: Can this be implemented?
internal actual fun isIntellijConsole(): Boolean = false

internal actual class AtomicInt actual constructor(initial: Int) {
    private val backing = kotlin.native.concurrent.AtomicInt(initial)
    actual fun getAndIncrement(): Int {
        return backing.value.also { backing.increment() }
    }
}

internal actual fun codepointSequence(string: String): Sequence<Int> = sequence {
    string.forEach { c ->
        val chars = c.toString().toCharArray()
        if (chars.size == 2) {
            check(Char.isSurrogatePair(chars[0], chars[1])) {
                "Expected surrogate pair"
            }
            yield(Char.toCodePoint(chars[0], chars[1]))
        } else {
            yield(chars.first().toInt())
        }
    }
}

internal actual fun String.toCodePoint(): Int {
    return if (length == 0) 0 else {
        toCharArray().run {
            if (size == 2) {
                check(Char.isSurrogatePair(get(0), get(1))) {
                    "Expected surrogate pair"
                }
                Char.toCodePoint(get(0), get(1))
            } else {
                first().toInt()
            }
        }
    }
}
