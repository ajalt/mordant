package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.getEnv
import com.github.ajalt.mordant.internal.runningInIdeaJavaAgent
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.AnsiLevel.*
import com.github.ajalt.mordant.rendering.Size

/**
 * Detect information about the current terminal.
 */
object TerminalDetection {
    /**
     * Detect information about the current terminal.
     *
     * @param ansiLevel The desired level of ANSI support. If `null`, the level will be detected.
     * @param hyperlinks Whether to enable hyperlink support. If `null`, hyperlinks support will be detected.
     * @param forceInputInteractive Force stdin to be treated as interactive. If `null`, stdin will be detected.
     * @param forceOutputInteractive Force stdout to be treated as interactive. If `null`, stdout will be detected.
     * @param detectedStdinInteractive Whether stdin was detected as interactive.
     * @param detectedStdoutInteractive Whether stdout was detected as interactive.
     */
    fun detectTerminal(
        ansiLevel: AnsiLevel?,
        hyperlinks: Boolean?,
        forceInputInteractive: Boolean?,
        forceOutputInteractive: Boolean?,
        detectedStdinInteractive: Boolean,
        detectedStdoutInteractive: Boolean,
    ): TerminalInfo {
        // intellij console is interactive, even though isatty returns false
        val ij = isIntellijRunActionConsole()
        val inputInteractive = forceInputInteractive ?: (ij || detectedStdinInteractive)
        val outputInteractive = forceOutputInteractive ?: (ij || detectedStdoutInteractive)
        val level = ansiLevel ?: ansiLevel(outputInteractive)
        val ansiHyperLinks = hyperlinks ?: (outputInteractive && level != NONE && ansiHyperLinks())
        return TerminalInfo(
            ansiLevel = level,
            ansiHyperLinks = ansiHyperLinks,
            outputInteractive = outputInteractive,
            inputInteractive = inputInteractive,
            supportsAnsiCursor = ij
        )
    }

    // https://gist.github.com/egmontkob/eb114294efbcd5adb1944c9f3cb5feda
    private fun ansiHyperLinks(): Boolean {
        if (forcedColor() == NONE || isCI()) return false
        if (isWindowsTerminal()) return true
        when (getTermProgram()) {
            "hyper", "wezterm" -> return true
            "iterm.app" -> return iTermVersionSupportsTruecolor()
            "mintty" -> return minttyVersionSupportsHyperlinks()
        }
        when (getTerm()) {
            "xterm-kitty", "alacritty" -> return true
        }
        return false
    }

    private fun ansiLevel(interactive: Boolean): AnsiLevel {
        forcedColor()?.let { return it }

        // Terminals embedded in some IDEs support color even though stdout isn't interactive. Check
        // those terminals before checking stdout.
        if (isIntellijRunActionConsole() || isVsCodeTerminal()) return TRUECOLOR

        // If output isn't interactive, never output colors, since we might be redirected to a file etc.
        if (!interactive) return NONE

        // Otherwise check the large variety of environment variables set by various terminal
        // emulators to detect color support

        if (isWindowsTerminal() || isDomTerm()) return TRUECOLOR

        if (isJediTerm()) return TRUECOLOR

        when (getColorTerm()) {
            "24bit", "24bits", "truecolor" -> return TRUECOLOR
        }

        if (isCI()) {
            return if (ciSupportsColor()) ANSI256 else NONE
        }

        when (getTermProgram()) {
            "hyper" -> return TRUECOLOR
            "apple_terminal" -> return ANSI256
            "iterm.app" -> return if (iTermVersionSupportsTruecolor()) TRUECOLOR else ANSI256
            "wezterm" -> return TRUECOLOR
            "mintty" -> return TRUECOLOR
        }

        val (term, level) = getTerm()?.split("-")
            ?.let { it.firstOrNull() to it.lastOrNull() }
            ?: (null to null)

        when (level) {
            "256", "256color", "256colors" -> return ANSI256
            "24bit", "24bits", "direct", "truecolor" -> return TRUECOLOR
        }


        // If there's no explicit level (like "xterm") or the level is ansi16 (like "rxvt-16color"),
        // guess the level based on the terminal.

        if (term == "xterm") {
            // Xterm sets an envvar with a version string that's "normally an identifier for the X
            // Window libraries used to build xterm, followed by xterm's patch number in
            // parentheses" https://linux.die.net/man/1/xterm
            //
            // 331 added truecolor support, 122 added 256 color support
            // https://invisible-island.net/xterm/xterm.log.html
            val xtermVersion = getEnv("XTERM_VERSION") ?: return ANSI16
            val m = Regex("""\((\d+)\)""").find(xtermVersion) ?: return ANSI16
            val v = m.groupValues[1].toInt()
            if (v >= 331) return TRUECOLOR
            if (v >= 122) return ANSI256
            return ANSI16
        }

        return when (term) {
            // New versions of Windows 10 cmd.exe supports truecolor, and most other terminal emulators
            // like ConEmu and mintty support truecolor, although they might downsample it.
            "cygwin" -> TRUECOLOR
            "vt100", "vt220", "screen", "tmux", "color", "linux", "ansi", "rxvt", "konsole" -> ANSI16
            else -> NONE
        }
    }

    private fun getTerm() = getEnv("TERM")?.lowercase()

    // https://github.com/termstandard/colors/
    private fun getColorTerm() = getEnv("COLORTERM")?.lowercase()

    private fun forcedColor(): AnsiLevel? = when {
        getTerm() == "dumb" -> NONE
        // https://no-color.org/
        getEnv("NO_COLOR") != null -> NONE
        // A lot of npm packages support the FORCE_COLOR envvar, although they all look for
        // different values. We try to support them all.
        else -> when (getEnv("FORCE_COLOR")?.lowercase()) {
            "0", "false", "none" -> NONE
            "1", "", "true", "16color" -> ANSI16
            "2", "256color" -> ANSI256
            "3", "truecolor" -> TRUECOLOR
            else -> null
        }
    }

    private fun getTermProgram() = getEnv("TERM_PROGRAM")?.lowercase()

    // https://github.com/Microsoft/vscode/pull/30346
    private fun isVsCodeTerminal() = getTermProgram() == "vscode"

    // https://github.com/microsoft/terminal/issues/1040#issuecomment-496691842
    private fun isWindowsTerminal() = !getEnv("WT_SESSION").isNullOrEmpty()

    // https://domterm.org/Detecting-domterm-terminal.html
    private fun isDomTerm() = !getEnv("DOMTERM").isNullOrEmpty()

    // https://github.com/JetBrains/intellij-community/blob/master/plugins/terminal/src/org/jetbrains/plugins/terminal/LocalTerminalDirectRunner.java#L141
    private fun isJediTerm() = getEnv("TERMINAL_EMULATOR") == "JetBrains-JediTerm"

    private fun iTermVersionSupportsTruecolor(): Boolean {
        val ver = getEnv("TERM_PROGRAM_VERSION")?.split(".")?.firstOrNull()?.toIntOrNull()
        return ver != null && ver >= 3
    }

    private fun minttyVersionSupportsHyperlinks(): Boolean {
        val ver = getEnv("TERM_PROGRAM_VERSION")?.split(".")?.mapNotNull { it.toIntOrNull() }
        if (ver?.size != 3) return false

        // https://github.com/mintty/mintty/issues/823#issuecomment-473096464
        return (ver[0] > 2) ||
                (ver[0] == 2 && ver[1] > 9) ||
                (ver[0] == 2 && ver[1] == 9 && ver[2] > 6)
    }

    private fun isCI(): Boolean {
        return getEnv("CI") != null
    }

    private fun ciSupportsColor(): Boolean {
        return listOf(
            "APPVEYOR",
            "BUILDKITE",
            "CIRCLECI",
            "DRONE",
            "GITHUB_ACTIONS",
            "GITLAB_CI",
            "TRAVIS",
        ).any { getEnv(it) != null }
    }

    private fun isIntellijRunActionConsole(): Boolean {
        // For some reason, IntelliJ's terminal behaves differently when running from an IDE run action vs running from
        // their terminal tab. In the latter case, the JediTerm envvar is set, in the former it's missing.
        return !isJediTerm() && runningInIdeaJavaAgent()
    }
}

private const val DEFAULT_WIDTH = 79
private const val DEFAULT_HEIGHT = 24
internal fun TerminalInterface.detectSize(
    info: TerminalInfo,
    width: Int?,
    height: Int?,
    nonInteractiveWidth: Int?,
    nonInteractiveHeight: Int?,
): Size {
    if (width != null && height != null) return Size(width, height)
    if (!info.outputInteractive) return Size(
        nonInteractiveWidth ?: width ?: DEFAULT_WIDTH,
        nonInteractiveHeight ?: height ?: DEFAULT_HEIGHT
    )
    val detected = getTerminalSize() ?: Size(
        width = (getEnv("COLUMNS")?.toIntOrNull() ?: DEFAULT_WIDTH),
        height = (getEnv("LINES")?.toIntOrNull() ?: DEFAULT_HEIGHT)
    )
    return Size(width = width ?: detected.width, height = height ?: detected.height)
}
