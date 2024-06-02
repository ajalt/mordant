package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.internal.syscalls.*
import com.github.ajalt.mordant.terminal.*
import java.io.File
import java.lang.management.ManagementFactory
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess

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

internal actual fun getSyscallHandler(): SyscallHandler {
    return System.getProperty("os.name").let { os ->
        try {
            // Inlined version of ImageInfo.inImageCode()
            val imageCode = System.getProperty("org.graalvm.nativeimage.imagecode")
            val isNativeImage = imageCode == "buildtime" || imageCode == "runtime"
            when {
                isNativeImage && os.startsWith("Windows") -> TODO("NativeImageWin32MppImpls()")
                isNativeImage && (os == "Linux" || os == "Mac OS X") -> TODO("NativeImagePosixMppImpls()")
                os.startsWith("Windows") -> SyscallHandlerJnaWindows
                os == "Linux" -> SyscallHandlerJnaLinux
                os == "Mac OS X" -> SyscallHandlerJnaMacos
                else -> DumbSyscallHandler
            }
        } catch (e: UnsatisfiedLinkError) {
            DumbSyscallHandler
        }
    }
}

internal actual val CR_IMPLIES_LF: Boolean = false
internal actual fun hasFileSystem(): Boolean = true

internal actual fun exitProcessMpp(status: Int) {
    exitProcess(status)
}

internal actual fun readFileIfExists(filename: String): String? {
    val file = File(filename)
    if (!file.isFile) return null
    return file.bufferedReader().use { it.readText() }
}
