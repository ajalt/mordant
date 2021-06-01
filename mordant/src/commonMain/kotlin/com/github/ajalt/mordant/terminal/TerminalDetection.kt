package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.*
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.AnsiLevel.*

internal object TerminalDetection {
    fun detectTerminal(
        ansiLevel: AnsiLevel?,
        width: Int?,
        height: Int?,
        hyperlinks: Boolean?,
        interactive: Boolean?,
    ): TerminalInfo {
        val stdoutInteractive = interactive ?: stdoutInteractive()
        val stdinInteractive = interactive ?: stdinInteractive()
        val level = ansiLevel ?: ansiLevel(stdoutInteractive)
        val ansiHyperLinks = hyperlinks ?: (stdoutInteractive && level != NONE && ansiHyperLinks())
        val (w, h) = detectInitialSize()
        return TerminalInfo(
            width = width ?: w,
            height = height ?: h,
            ansiLevel = level,
            ansiHyperLinks = ansiHyperLinks,
            stdoutInteractive = stdoutInteractive,
            stdinInteractive = stdinInteractive
        )
    }

    /** Returns pair of [width, height], or null if it can't be detected */
    fun detectSize(timeoutMs: Long): Pair<Int, Int>? = getTerminalSize(timeoutMs)

    private fun detectInitialSize(): Pair<Int, Int> {
        val detected = when {
            terminalSizeDetectionIsFast() -> getTerminalSize(100)
            else -> null
        }
        return detected ?: (getEnv("COLUMNS")?.toInt() ?: 79) to (getEnv("LINES")?.toInt() ?: 24)
    }

    // https://gist.github.com/egmontkob/eb114294efbcd5adb1944c9f3cb5feda
    private fun ansiHyperLinks(): Boolean {
        return forcedColor() != NONE && (isWindowsTerminal() || when (getTermProgram()) {
            "hyper", "wezterm" -> true
            "iterm.app" -> isRecentITerm()
            else -> false
        })
    }


    private fun ansiLevel(stdoutInteractive: Boolean): AnsiLevel {
        forcedColor()?.let { return it }

        // Terminals embedded in some IDEs support color even though stdout isn't interactive. Check
        // those terminals before checking stdout.
        if (isIntellijConsole() || isVsCodeTerminal()) return TRUECOLOR

        // If stdout isn't interactive, never output colors, since we might be redirected to a file etc.
        if (!stdoutInteractive) return NONE

        // Otherwise check the large variety of environment variables set by various terminal
        // emulators to detect color support

        if (isWindowsTerminal() || isDomTerm()) return TRUECOLOR

        when (getColorTerm()) {
            "24bit", "24bits", "truecolor" -> return TRUECOLOR
        }

        if (isCI()) {
            return if (ciSupportsColor()) ANSI256 else NONE
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
                getJavaProperty("os.name") == "Windows 10" -> TRUECOLOR
                else -> ANSI256
            }
            "xterm", "vt100", "vt220", "screen", "color", "linux", "ansi", "rxvt", "konsole" -> ANSI16
            "dumb" -> NONE
            else -> NONE
        }
    }

    private fun getTerm() = getEnv("TERM")?.lowercase()

    // https://github.com/termstandard/colors/
    private fun getColorTerm() = getEnv("COLORTERM")?.lowercase()

    private fun forcedColor(): AnsiLevel? {
        return when {
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
    }

    private fun getTermProgram() = getEnv("TERM_PROGRAM")?.lowercase()

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

    private fun isIntellijConsole(): Boolean {
        return hasIdeaEnvvar() || runningInIdeaJavaAgent()
    }

    // Some versions of IntelliJ set various environment variables
    private fun hasIdeaEnvvar(): Boolean {
        return getEnv("IDEA_INITIAL_DIRECTORY") != null
                || getEnv("__INTELLIJ_COMMAND_HISTFILE__") != null
                || getEnv("TERMINAL_EMULATOR")?.contains("jetbrains", ignoreCase = true) == true
                || getJavaProperty("sun.java.command")?.contains("idea", ignoreCase = true) == true
    }
}
