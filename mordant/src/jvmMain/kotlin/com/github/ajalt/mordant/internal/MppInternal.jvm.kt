package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.input.InputEvent
import com.github.ajalt.mordant.input.MouseTracking
import com.github.ajalt.mordant.rendering.Size
import com.github.ajalt.mordant.terminal.*
import java.io.IOException
import java.lang.management.ManagementFactory
import java.nio.file.Path
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.readText
import kotlin.system.exitProcess
import kotlin.time.TimeMark

private class JvmAtomicRef<T>(value: T) : MppAtomicRef<T> {
    private val ref = AtomicReference(value)
    override val value: T
        get() = ref.get()

    override fun compareAndSet(expected: T, newValue: T): Boolean {
        return ref.compareAndSet(expected, newValue)
    }

    override fun getAndSet(newValue: T): T {
        return ref.getAndSet(newValue)
    }
}

private class JvmAtomicInt(initial: Int) : MppAtomicInt {
    private val backing = AtomicInteger(initial)
    override fun getAndIncrement(): Int {
        return backing.getAndIncrement()
    }

    override fun get(): Int {
        return backing.get()
    }

    override fun set(value: Int) {
        backing.set(value)
    }
}

internal actual fun MppAtomicInt(initial: Int): MppAtomicInt = JvmAtomicInt(initial)
internal actual fun <T> MppAtomicRef(value: T): MppAtomicRef<T> = JvmAtomicRef(value)

internal actual fun getEnv(key: String): String? = System.getenv(key)

// Depending on how IntelliJ is configured, it might use its own Java agent
internal actual fun runningInIdeaJavaAgent() = try {
    val bean = ManagementFactory.getRuntimeMXBean()
    val jvmArgs = bean.inputArguments
    jvmArgs.any { it.startsWith("-javaagent") && "idea_rt.jar" in it }
} catch (e: Throwable) {
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

internal actual fun makePrintingTerminalCursor(terminal: Terminal): TerminalCursor {
    return JvmTerminalCursor(terminal)
}

private class JvmTerminalCursor(terminal: Terminal) : PrintTerminalCursor(terminal) {
    private var shutdownHook: Thread? = null
    private val cursorLock = Any()

    override fun show() {
        synchronized(cursorLock) {
            shutdownHook?.let { hook ->
                Runtime.getRuntime().removeShutdownHook(hook)
            }
        }
        super.show()
    }

    override fun hide(showOnExit: Boolean) {
        if (showOnExit) {
            synchronized(cursorLock) {
                if (shutdownHook == null) {
                    shutdownHook = Thread { super.show() }
                    Runtime.getRuntime().addShutdownHook(shutdownHook)
                }
            }
        }
        super.hide(showOnExit)
    }
}

private val printRequestLock = Any()

internal actual fun sendInterceptedPrintRequest(
    request: PrintRequest,
    terminalInterface: TerminalInterface,
    interceptors: List<TerminalInterceptor>,
) = synchronized(printRequestLock) {
    terminalInterface.completePrintRequest(interceptors.fold(request) { acc, it ->
        it.intercept(acc)
    })
}

internal actual fun getStandardTerminalInterface(): TerminalInterface {
    val providers = ServiceLoader.load(TerminalInterfaceProvider::class.java)
        .associateBy { it::class.qualifiedName }
    // The built-in providers in the order that they should be loaded
    val builtins = listOf(
        "com.github.ajalt.mordant.terminal.terminalinterface.ffm.TerminalInterfaceProviderFfm",
        "com.github.ajalt.mordant.terminal.terminalinterface.nativeimage.TerminalInterfaceProviderNativeImage",
        "com.github.ajalt.mordant.terminal.terminalinterface.jna.TerminalInterfaceProviderJna",
    )

    // All providers, including user-provided ones
    val allProviders = builtins + (providers.keys - builtins)

    for (provider in allProviders) {
        return providers[provider]?.load() ?: continue
    }

    return DumbTerminalInterface
}

internal actual val CR_IMPLIES_LF: Boolean = false
internal actual fun testsHaveFileSystem(): Boolean = true

internal actual fun exitProcessMpp(status: Int) {
    exitProcess(status)
}

internal actual fun readFileIfExists(filename: String): String? {
    return try {
        Path.of(filename).readText()
    } catch (e: IOException) {
        null
    }
}

private const val DUMB_RAW_MODE_ERROR = """Cannot find terminal interface that supports raw mode.

You need at least one of the `:mordant-jvm-*` modules on your classpath.
The `:mordant` module includes all of them as transitive dependencies.
If you're using `:mordant-core` instead, you need to add one or more manually.
If you're using only `:mordant-jvm-ffm`, make sure you're running with JVM 22+, and are passing
`--enable-native-access=ALL-UNNAMED` as a JVM argument.
"""

private object DumbTerminalInterface : StandardTerminalInterface() {
    override fun shouldAutoUpdateSize(): Boolean = false
    override fun getTerminalSize(): Size? = null
    override fun enterRawMode(mouseTracking: MouseTracking): AutoCloseable {
        throw UnsupportedOperationException(DUMB_RAW_MODE_ERROR)
    }

    override fun readInputEvent(timeout: TimeMark, mouseTracking: MouseTracking): InputEvent? {
        throw UnsupportedOperationException(DUMB_RAW_MODE_ERROR)
    }
}
