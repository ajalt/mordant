package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.rendering.AnsiLevel
import java.io.File
import java.io.Writer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Hide the cursor from a shutdown hook of the program's own, after the JVM has started shutting
 * down, when nothing is left to show it again:
 *
 * - `unregistered`: nothing hid the cursor before, so the cursor's hook can't be registered anymore
 * - `hook-ran`: the cursor was hidden and shown before, and the program's hook waits until the
 *   cursor's hook has shown it once more
 *
 * `JvmTerminalCursorTest` runs this in a subprocess with the mode in `args[1]`, and reads the file
 * named by `args[0]`.
 */
internal object CursorShutdownProgram {
    @JvmStatic
    fun main(args: Array<String>) {
        val hookRan = args[1] == "hook-ran"
        val shown = CountDownLatch(if (hookRan) 2 else 0)
        val terminal = Terminal(
            ansiLevel = AnsiLevel.ANSI16,
            interactive = true,
            terminalInterface = ShowCountingTerminalInterface(File(args[0]).bufferedWriter(), shown),
        )
        if (hookRan) {
            terminal.cursor.hide(showOnExit = true)
            terminal.cursor.show()
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            shown.await(10, TimeUnit.SECONDS)
            terminal.cursor.hide(showOnExit = true)
        })
    }
}

/** Write every request through immediately, and count down [shown] on each show-cursor sequence */
private class ShowCountingTerminalInterface(
    private val writer: Writer,
    private val shown: CountDownLatch,
) : StandardTerminalInterface() {
    override fun completePrintRequest(request: PrintRequest) {
        synchronized(writer) {
            writer.write(request.text)
            writer.flush()
        }
        if (request.text == "$CSI?25h") shown.countDown()
    }
}
