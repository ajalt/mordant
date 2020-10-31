package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.DEFAULT_THEME
import com.github.ajalt.mordant.rendering.Lines
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.internal.renderLinesAnsi


class VirtualTerminal(
        ansiLevel: AnsiLevel = AnsiLevel.TRUECOLOR,
        override val theme: Theme = DEFAULT_THEME,
        width: Int = 79,
        height: Int = 24,
        hyperlinks: Boolean = ansiLevel != AnsiLevel.NONE,
        override val tabWidth: Int = 4,
        stdoutInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
        stdinInteractive: Boolean = ansiLevel != AnsiLevel.NONE,
) : Terminal {
    init {
        require(tabWidth >= 0) { "tab width cannot be negative" }
    }

    private val sb = StringBuilder()

    fun clearBuffer() {
        sb.clear()
    }

    fun buffer(): String = sb.toString()

    override val info = TerminalInfo(width, height, ansiLevel, hyperlinks, stdoutInteractive = stdoutInteractive, stdinInteractive = stdinInteractive)
    override val colors: TerminalColors = TerminalColors(info.ansiLevel)
    override val cursor: TerminalCursor = if (stdinInteractive && stdinInteractive) PrintTerminalCursor(this) else DisabledTerminalCursor

    override fun render(lines: Lines): String {
        return renderLinesAnsi(lines, info.ansiLevel, info.ansiHyperLinks)
    }

    override fun println() {
        sb.append("\n")
    }

    override fun rawPrintln(message: String) {
        sb.append(message).append("\n")
    }

    override fun rawPrint(message: String) {
        sb.append(message)
    }
}

