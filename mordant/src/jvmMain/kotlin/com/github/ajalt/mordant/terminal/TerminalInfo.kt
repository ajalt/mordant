package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.AnsiLevel


interface TerminalInfo {
    val width: Int
    val ansiLevel: AnsiLevel
    val ansiHyperLinks: Boolean
    val stdoutInteractive: Boolean
    val stdinInteractive: Boolean

    /** Return true if both stdin and stdout are interactive */
    val interactive: Boolean get() = stdinInteractive && stdinInteractive
}

internal data class StaticTerminalInfo(
        override val width: Int,
        override val ansiLevel: AnsiLevel,
        override val ansiHyperLinks: Boolean,
        override val stdoutInteractive: Boolean,
        override val stdinInteractive: Boolean
): TerminalInfo
