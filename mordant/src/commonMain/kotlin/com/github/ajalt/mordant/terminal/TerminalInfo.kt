package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.AnsiLevel


class TerminalInfo(
    width: Int,
    height: Int,
    var ansiLevel: AnsiLevel,
    var ansiHyperLinks: Boolean,
    val stdoutInteractive: Boolean,
    val stdinInteractive: Boolean,
    val stderrInteractive: Boolean,
) {
    var width: Int = width
        private set
    var height: Int = height
        private set


    /** Return true if both stdin and stdout are interactive */
    val interactive: Boolean get() = stdinInteractive && stdinInteractive

    /**
     * Query the terminal for its current size, updating [width] and [height] if successful.
     *
     * On JVM, this call will create a subprocess and block for up to [timeoutMs] waiting for it to complete.
     * On other platforms, this call doesn't require a subprocess, and [timeoutMs] is ignored.
     *
     * @return `true` if the size was updated, of `false` if it was not
     */
    fun updateTerminalSize(timeoutMs: Long = 5000): Boolean {
        val (w, h) = TerminalDetection.detectSize(timeoutMs) ?: return false
        width = w
        height = h
        return true
    }
}
