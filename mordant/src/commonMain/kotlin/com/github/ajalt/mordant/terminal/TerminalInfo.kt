package com.github.ajalt.mordant.terminal


class TerminalInfo(
        width: Int,
        height: Int,
        val ansiLevel: AnsiLevel,
        val ansiHyperLinks: Boolean,
        val stdoutInteractive: Boolean,
        val stdinInteractive: Boolean,
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
     * This call will create subprocess and block for up to [timeoutMs] waiting for it to complete.
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
