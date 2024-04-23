package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.internal.jna.JnaLinuxMppImpls
import com.github.ajalt.mordant.internal.jna.JnaMacosMppImpls
import com.github.ajalt.mordant.internal.jna.JnaWin32MppImpls
import com.github.ajalt.mordant.internal.nativeimage.NativeImagePosixMppImpls
import com.github.ajalt.mordant.internal.nativeimage.NativeImageWin32MppImpls
import com.github.ajalt.mordant.terminal.*
import java.lang.management.ManagementFactory
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

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

private val impls: MppImpls = System.getProperty("os.name").let { os ->
    try {
        // Inlined version of ImageInfo.inImageCode()
        val imageCode = System.getProperty("org.graalvm.nativeimage.imagecode")
        val isNativeImage = imageCode == "buildtime" || imageCode == "runtime"
        when {
            isNativeImage && os.startsWith("Windows") -> NativeImageWin32MppImpls()
            isNativeImage && (os == "Linux" || os == "Mac OS X") -> NativeImagePosixMppImpls()
            os.startsWith("Windows") -> JnaWin32MppImpls()
            os == "Linux" -> JnaLinuxMppImpls()
            os == "Mac OS X" -> JnaMacosMppImpls()
            else -> FallbackMppImpls()
        }
    } catch (e: UnsatisfiedLinkError) {
        FallbackMppImpls()
    }
}

internal actual fun stdoutInteractive(): Boolean = impls.stdoutInteractive()
internal actual fun stdinInteractive(): Boolean = impls.stdinInteractive()
internal actual fun getTerminalSize(): Size? = impls.getTerminalSize()
internal actual val FAST_ISATTY: Boolean = true
internal actual val CR_IMPLIES_LF: Boolean = false
