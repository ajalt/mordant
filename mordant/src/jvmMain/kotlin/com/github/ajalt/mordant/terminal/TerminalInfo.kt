package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.AnsiLevel


interface TerminalInfo {
    val width: Int
    // TODO: terminal height
    val ansiLevel: AnsiLevel
    val ansiHyperLinks: Boolean
    val stdoutInteractive: Boolean
    val stdinInteractive: Boolean
}

internal data class StaticTerminalInfo(
        override val width: Int,
        override val ansiLevel: AnsiLevel,
        override val ansiHyperLinks: Boolean,
        override val stdoutInteractive: Boolean,
        override val stdinInteractive: Boolean
): TerminalInfo
