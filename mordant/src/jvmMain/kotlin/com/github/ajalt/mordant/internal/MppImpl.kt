package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.*
import java.io.Console
import java.io.IOException
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal actual class AtomicInt actual constructor(initial: Int) {
    private val backing = AtomicInteger(initial)
    actual fun getAndIncrement(): Int {
        return backing.getAndIncrement()
    }

    actual fun get(): Int {
        return backing.get()
    }

    actual fun set(value: Int) {
        backing.set(value)
    }
}

// We have to shell out to another program on JVM, which takes ~10ms for stty and ~100ms for powershell
internal actual fun terminalSizeDetectionIsFast(): Boolean = false

private fun runCommand(vararg args: String): Process? {
    return try {
        ProcessBuilder(*args)
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .start()
    } catch (e: IOException) {
        null
    }
}

private fun parseWindowsPowershellSize(output: String): Pair<Int, Int>? {
    val groups = Regex("""[Ww]indow[Ss]ize\s+:?\s+(\d+),(\d+)""").find(output)?.groupValues ?: return null
    return groups[1].toInt() to groups[2].toInt()
}

private fun parseSttySize(output: String): Pair<Int, Int>? {
    val dimens = output.split(" ").mapNotNull { it.toIntOrNull() }
    if (dimens.size != 2) return null
    return dimens[1] to dimens[0]
}


internal actual fun getEnv(key: String): String? = System.getenv(key)

// Depending on how IntelliJ is configured, it might use its own Java agent
internal actual fun runningInIdeaJavaAgent() = try {
    val bean = ManagementFactory.getRuntimeMXBean()
    val jvmArgs = bean.inputArguments
    jvmArgs.any { it.startsWith("-javaagent") && "idea_rt.jar" in it }
} catch (e: SecurityException) {
    false
}



internal actual fun codepointSequence(string: String): Sequence<Int> {
    return string.codePoints().iterator().asSequence()
}

internal actual fun printStderr(message: String, newline: Boolean) {
    if (newline) {
        System.err.println(message)
    } else {
        System.err.print(message)
    }
}

internal actual fun readLineOrNullMpp(hideInput: Boolean): String? {
    if (hideInput) {
        val console = System.console()
        if (console != null) {
            return console.readPassword().concatToString()
        }
    }
    return readlnOrNull()
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
                    shutdownHook = Thread { super.show() }
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
) {
    terminalInterface.completePrintRequest(
        interceptors.fold(request) { acc, it -> it.intercept(acc) }
    )
}

internal actual inline fun synchronizeJvm(lock: Any, block: () -> Unit) = synchronized(lock, block)

private val impls : JnaMppImpls = System.getProperty("os.name").let { os ->
    when {
        os.startsWith("Windows") -> Win32MppImpls()
        else -> TODO()
    }
}

internal actual fun stdoutInteractive(): Boolean = impls.stdoutInteractive()
internal actual fun stdinInteractive(): Boolean = impls.stdinInteractive()
internal actual fun stderrInteractive(): Boolean = impls.stderrInteractive()
internal actual fun getTerminalSize(timeoutMs: Long): Pair<Int, Int>? = impls.getTerminalSize()
