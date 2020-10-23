package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.AnsiLevel
import com.github.ajalt.mordant.AnsiLevel.*
import java.lang.management.ManagementFactory

internal object TerminalDetection {
    fun detectTerminal(
            ansiLevel: AnsiLevel? = null,
            width: Int? = null,
            hyperlinks: Boolean? = null
    ): TerminalInfo {
        return StaticTerminalInfo(
                width = width ?: width() ?: 79,
                ansiLevel = ansiLevel ?: ansiLevel(),
                ansiHyperLinks = hyperlinks ?: ansiHyperLinks(),
                stdoutInteractive = stdoutInteractive(),
                stdinInteractive = stdinInteractive()
        )
    }

    private fun width(): Int? = System.getenv("COLUMNS")?.toInt()

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
        if (isIntellijConsole()) return ANSI16
        if (isVsCodeTerminal()) return ANSI256

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

        val (term, level) = System.getenv("TERM")?.toLowerCase()?.split("-")
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
                System.getProperty("os.name") == "Windows 10" -> TRUECOLOR
                else -> ANSI256
            }
            "xterm", "vt100", "vt220", "screen", "ansi", "rxvt", "konsole" -> ANSI16
            "dumb" -> NONE
            else -> NONE
        }
    }

    // https://github.com/termstandard/colors/
    private fun getColorTerm() = System.getenv("COLORTERM")?.toLowerCase()

    private fun forcedColor(): AnsiLevel? {
        return when {
            System.getenv("NO_COLOR") != null -> NONE // https://no-color.org/
            else -> when (System.getenv("FORCE_COLOR")) {
                "0", "false", "none" -> NONE
                "1", "", "true", "16color" -> ANSI16
                "2", "256color" -> ANSI256
                "3", "truecolor" -> TRUECOLOR
                else -> null
            }
        }
    }

    private fun getTermProgram() = System.getenv("TERM_PROGRAM")?.toLowerCase()

    // https://github.com/Microsoft/vscode/pull/30346
    private fun isVsCodeTerminal() = getTermProgram() == "vscode"


    // https://github.com/microsoft/terminal/issues/1040#issuecomment-496691842
    private fun isWindowsTerminal() = !System.getenv("WT_SESSION").isNullOrEmpty()

    // https://domterm.org/Detecting-domterm-terminal.html
    private fun isDomTerm() = !System.getenv("DOMTERM").isNullOrEmpty()

    private fun isRecentITerm(): Boolean {
        val ver = System.getenv("TERM_PROGRAM_VERSION")?.split(".")?.firstOrNull()?.toIntOrNull()
        return ver != null && ver >= 3
    }

    // Unfortunately, the JVM doesn't let us check stdin and stdout separately
    private fun stdoutInteractive(): Boolean = System.console() != null
    private fun stdinInteractive(): Boolean = System.console() != null

    // Consoles built in to some IDEs/Editors support color, but always cause System.console() to
    // return null
    private fun isIntellijConsole(): Boolean {
        return try {
            val bean = ManagementFactory.getRuntimeMXBean()
            val jvmArgs = bean.inputArguments
            jvmArgs.any { it.startsWith("-javaagent") && "idea_rt.jar" in it }
        } catch (e: SecurityException) {
            false
        }
    }
}
