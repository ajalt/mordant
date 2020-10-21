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
        return isWindowsTerminal() || when (getTermProgram()) {
            "hyper", "wezterm" -> true
            "iterm.app" -> isRecentITerm()
            else -> false
        }
    }

    private fun ansiLevel(): AnsiLevel {
        if (isIntellijConsole()) return ANSI16
        if (isVsCodeTerminal()) return ANSI256
        if (isWindowsTerminal() || isDomTerm()) return TRUECOLOR

        // https://github.com/termstandard/colors/
        when (System.getenv("COLORTERM")?.toLowerCase()) {
            "24bit", "24bits", "truecolor" -> return TRUECOLOR
        }

        when (getTermProgram()) {
            "hyper" -> return TRUECOLOR
            "apple_terminal" -> return ANSI256
            "iterm.app" -> return if (isRecentITerm()) TRUECOLOR else ANSI256
            "wezterm" -> return TRUECOLOR
        }

        val term = System.getenv("TERM")?.toLowerCase()
        when (term?.split("-")?.takeIf { it.size > 1 }?.last()) {
            "256", "256color", "256colors" -> return ANSI256
            "24bit", "24bits", "direct", "truecolor" -> return TRUECOLOR
        }

        return when (term) {
            "cygwin" -> when {
                // New versions of windows 10 cmd.exe supports truecolor, and most other terminal emulators
                // like ConEmu and mintty support truecolor, although they might downsample it.
                System.getProperty("os.name") == "Windows 10" -> TRUECOLOR
                else -> ANSI256
            }
            "xterm", "vt100", "vt220", "screen", "ansi", "rxvt" -> ANSI16
            "dumb" -> NONE
            else -> NONE
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
