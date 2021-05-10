package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.*
import java.io.IOException
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal actual class AtomicInt actual constructor(initial: Int) {
    private val backing = AtomicInteger(initial)
    actual fun getAndIncrement(): Int {
        return backing.getAndIncrement()
    }
}

// We have to shell out to another program on JVM, which takes ~10ms for stty and ~100ms for powershell
internal actual fun terminalSizeDetectionIsFast(): Boolean = false

internal actual fun getTerminalSize(timeoutMs: Long): Pair<Int, Int>? {
    val process = try {
        val cmd = when {
            isWindows() -> ProcessBuilder("powershell.exe",
                "-noprofile",
                "-command",
                "\$host.ui.rawui")
            else -> ProcessBuilder("stty", "size")
        }
        cmd.redirectInput(ProcessBuilder.Redirect.INHERIT)
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

    val output = process.inputStream.bufferedReader().readText().trim()
    return when {
        isWindows() -> parseWindowsPowershellSize(output)
        else -> parseSttySize(output)
    }
}

private fun parseWindowsPowershellSize(output: String): Pair<Int, Int>? {
    val groups = Regex("""[Ww]window[Ss]ize\s+:?\s+(\d+),(\d+)""").find(output)?.groupValues ?: return null
    return groups[1].toInt() to groups[2].toInt()
}

private fun parseSttySize(output: String): Pair<Int, Int>? {
    val dimens = output.split(" ").mapNotNull { it.toIntOrNull() }
    if (dimens.size != 2) return null
    return dimens[1] to dimens[0]
}

internal actual fun isWindows(): Boolean = "win" in System.getProperty("os.name").lowercase()

internal actual fun getEnv(key: String): String? = System.getenv(key)

internal actual fun getJavaProperty(key: String): String? = System.getProperty(key)

// Depending on how IntelliJ is configured, it might use its own Java agent
internal actual fun runningInIdeaJavaAgent() = try {
    val bean = ManagementFactory.getRuntimeMXBean()
    val jvmArgs = bean.inputArguments
    jvmArgs.any { it.startsWith("-javaagent") && "idea_rt.jar" in it }
} catch (e: SecurityException) {
    false
}

// Unfortunately, the JVM doesn't let us check stdin and stdout separately.
internal actual fun stdoutInteractive(): Boolean = System.console() != null

internal actual fun stdinInteractive(): Boolean = System.console() != null

internal actual fun codepointSequence(string: String): Sequence<Int> {
    return string.codePoints().iterator().asSequence()
}

internal actual fun makePrintingTerminalCursor(terminal: Terminal): TerminalCursor = JvmTerminalCursor(terminal)

private class JvmTerminalCursor(terminal: Terminal) : PrintTerminalCursor(terminal) {
    private var shutdownHook: Thread? = null
    private val lock = Any()

    override fun show() {
        synchronized(lock) {
            shutdownHook?.let { hook ->
                Runtime.getRuntime().removeShutdownHook(hook)
            }
        }
        super.show()
    }

    override fun hide(showOnExit: Boolean) {
        if (showOnExit) {
            synchronized(lock) {
                if (shutdownHook == null) {
                    shutdownHook = Thread { show() }
                    Runtime.getRuntime().addShutdownHook(shutdownHook)
                }
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
    lock: Any,
) {
    synchronized(lock) {
        terminalInterface.completePrintRequest(
            interceptors.fold(request) { acc, it -> it.intercept(acc) }
        )
    }
}
