package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.getEnv
import com.github.ajalt.mordant.getProperty
import com.github.ajalt.mordant.getTerminalSize
import com.github.ajalt.mordant.isIntellijConsole
import com.github.ajalt.mordant.stdinInteractive
import com.github.ajalt.mordant.stdoutInteractive
import com.github.ajalt.mordant.terminal.AnsiLevel.*

internal object TerminalDetection {
    fun detectTerminal(
            ansiLevel: AnsiLevel? = null,
            width: Int? = null,
            height: Int? = null,
            hyperlinks: Boolean? = null
    ): TerminalInfo = TerminalInfo(
            width = width ?: width() ?: 79,
            height = height ?: height() ?: 24,
            ansiLevel = ansiLevel ?: ansiLevel(),
            ansiHyperLinks = hyperlinks ?: ansiHyperLinks(),
            stdoutInteractive = stdoutInteractive(),
            stdinInteractive = stdinInteractive()
    )

    fun detectSize(timeoutMs: Long): Pair<Int, Int>? =
        getTerminalSize(timeoutMs)

    private fun width(): Int? = getEnv("COLUMNS")?.toInt()
    private fun height(): Int? = getEnv("LINES")?.toInt()

    // https://gist.github.com/egmontkob/eb114294efbcd5adb1944c9f3cb5feda
    private fun ansiHyperLinks(): Boolean {
        return forcedColor() != NONE && (isWindowsTerminal() || when (getTermProgram()) {
            "hyper", "wezterm" -> true
            "iterm.app" -> isRecentITerm()
            else -> false
        })
    }


    private fun ansiLevel(): AnsiLevel {
        forcedColor()?.let { return it }

        // Terminals embedded in some IDEs support color even though stdout isn't interactive. Check
        // those terminals before checking stdout.
        if (isIntellijConsole() || isVsCodeTerminal()) return TRUECOLOR

        // If stdout isn't interactive, never output colors, since we might be redirected to a file etc.
        if (!stdoutInteractive()) return NONE

        // Otherwise check the large variety of environment variables set by various terminal
        // emulators to detect color support

        if (isWindowsTerminal() || isDomTerm()) return TRUECOLOR

        when (getColorTerm()) {
            "24bit", "24bits", "truecolor" -> return TRUECOLOR
        }

        when (getTermProgram()) {
            "hyper" -> return TRUECOLOR
            "apple_terminal" -> return ANSI256
            "iterm.app" -> return if (isRecentITerm()) TRUECOLOR else ANSI256
            "wezterm" -> return TRUECOLOR
        }

        val (term, level) = getTerm()?.split("-")
                ?.let { it.firstOrNull() to it.lastOrNull() }
                ?: null to null

        when (level) {
            "256", "256color", "256colors" -> return ANSI256
            "24bit", "24bits", "direct", "truecolor" -> return TRUECOLOR
        }

        // If there's no explicit level (like "xterm") or the level is ansi16 (like "rxvt-16color"),
        // just look at the terminal value
        return when (term) {
            "cygwin" -> when {
                // New versions of windows 10 cmd.exe supports truecolor, and most other terminal emulators
                // like ConEmu and mintty support truecolor, although they might downsample it.
                getProperty("os.name") == "Windows 10" -> TRUECOLOR
                else -> ANSI256
            }
            "xterm", "vt100", "vt220", "screen", "color", "linux", "ansi", "rxvt", "konsole" -> ANSI16
            "dumb" -> NONE
            else -> NONE
        }
    }

    private fun getTerm() = getEnv("TERM")?.toLowerCase()

    // https://github.com/termstandard/colors/
    private fun getColorTerm() = getEnv("COLORTERM")?.toLowerCase()

    private fun forcedColor(): AnsiLevel? {
        return when {
            getTerm() == "dumb" -> NONE
            // https://no-color.org/
            getEnv("NO_COLOR") != null -> NONE
            // A lot of npm packages support the FORCE_COLOR envvar, although they all look for
            // different values. We try to support them all.
            else -> when (getEnv("FORCE_COLOR")?.toLowerCase()) {
                "0", "false", "none" -> NONE
                "1", "", "true", "16color" -> ANSI16
                "2", "256color" -> ANSI256
                "3", "truecolor" -> TRUECOLOR
                else -> null
            }
        }
    }

    private fun getTermProgram() = getEnv("TERM_PROGRAM")?.toLowerCase()

    // https://github.com/Microsoft/vscode/pull/30346
    private fun isVsCodeTerminal() = getTermProgram() == "vscode"

    // https://github.com/microsoft/terminal/issues/1040#issuecomment-496691842
    private fun isWindowsTerminal() = !getEnv("WT_SESSION").isNullOrEmpty()

    // https://domterm.org/Detecting-domterm-terminal.html
    private fun isDomTerm() = !getEnv("DOMTERM").isNullOrEmpty()

    private fun isRecentITerm(): Boolean {
        val ver = getEnv("TERM_PROGRAM_VERSION")?.split(".")?.firstOrNull()?.toIntOrNull()
        return ver != null && ver >= 3
    }
}
