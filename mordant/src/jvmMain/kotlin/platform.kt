package com.github.ajalt.mordant

import java.io.IOException
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


internal actual fun getTerminalSize(timeoutMs: Long): Pair<Int, Int>? {
    val process = try {
        ProcessBuilder("stty", "size")
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .start()
    } catch (e: IOException) {
        return null
    }
    try {
        if (!process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)) {
            return null
        }
    } catch (e: InterruptedException) {
        return null
    }

    val output = process.inputStream.use { it.bufferedReader().readText() }
    val dimens = output.trim().split(" ").mapNotNull { it.toIntOrNull() }
    if (dimens.size != 2) return null
    return dimens[1] to dimens[0]
}

internal actual fun getProperty(key: String): String? =
        System.getProperty(key)

internal actual fun getEnv(key: String): String? =
        System.getenv(key)

// Unfortunately, the JVM doesn't let us check stdin and stdout separately. The IntelliJ console
// is interactive even though it redirects stdin.
internal actual fun stdoutInteractive(): Boolean =
        System.console() != null || isIntellijConsole()

internal actual fun stdinInteractive(): Boolean =
        System.console() != null || isIntellijConsole()

internal actual fun isIntellijConsole(): Boolean = try {
    val bean = ManagementFactory.getRuntimeMXBean()
    val jvmArgs = bean.inputArguments
    jvmArgs.any { it.startsWith("-javaagent") && "idea_rt.jar" in it }
} catch (e: SecurityException) {
    false
}

internal actual class AtomicInt actual constructor(initial: Int) {
    private val backing = AtomicInteger(initial)
    actual fun getAndIncrement(): Int {
        return backing.getAndIncrement()
    }
}

internal actual fun codepointSequence(string: String): Sequence<Int> =
        string.codePoints().iterator().asSequence()

internal actual fun String.toCodePoint(): Int = codePointAt(0)
