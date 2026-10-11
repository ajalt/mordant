package com.github.ajalt.mordant.animation.progress

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.PrintRequest
import com.github.ajalt.mordant.terminal.StandardTerminalInterface
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.progress.completed
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import java.io.File
import java.io.Writer

/**
 * Draw an unfinished progress bar from [execute]'s thread, then kill the main thread so that the
 * JVM starts shutting down while the animation is still running.
 *
 * `ThreadAnimatorTest` runs this in a subprocess and reads the file named by `args[0]`. The output
 * can't go to stdout: a redirected `System.out` is block buffered, and whatever is still in the
 * buffer when the JVM halts is lost, which is exactly the window being measured.
 */
internal object AnimatorShutdownProgram {
    @JvmStatic
    fun main(args: Array<String>) {
        val terminal = Terminal(
            ansiLevel = AnsiLevel.ANSI16,
            width = 56,
            interactive = true,
            terminalInterface = FlushingTerminalInterface(File(args[0]).bufferedWriter()),
        )
        val animator = progressBarLayout { completed(fps = 60) }
            .animateOnThread(terminal, total = 100)
        animator.execute()
        animator.advance(1)
        Thread.sleep(200)
        // The stack trace goes to stderr, leaving the file to the animation
        throw RuntimeException("killing the main thread")
    }
}

/** Write every request through immediately, so that a halting JVM can't drop one. */
private class FlushingTerminalInterface(private val writer: Writer) : StandardTerminalInterface() {
    override fun completePrintRequest(request: PrintRequest) {
        synchronized(writer) {
            writer.write(request.text)
            if (request.trailingLinebreak) writer.write("\n")
            writer.flush()
        }
    }
}
