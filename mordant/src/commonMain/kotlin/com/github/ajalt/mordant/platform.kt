package com.github.ajalt.mordant

internal expect fun getTerminalSize(timeoutMs: Long): Pair<Int, Int>?

internal expect fun getProperty(key: String): String?

internal expect fun getEnv(key: String): String?

internal expect fun stdoutInteractive(): Boolean

internal expect fun stdinInteractive(): Boolean

// Consoles built in to some IDEs/Editors support color, but always cause System.console() to
// return null
internal expect fun isIntellijConsole(): Boolean

internal expect class AtomicInt(initial: Int) {
    fun getAndIncrement(): Int
}

internal expect fun codepointSequence(string: String): Sequence<Int>

internal expect fun String.toCodePoint(): Int
