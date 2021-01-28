package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.AnsiLevel.*
import java.io.IOException
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

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
        return TerminalInfo(
            width = width ?: width() ?: 79,
            height = height ?: height() ?: 24,
            ansiLevel = level,
            ansiHyperLinks = ansiHyperLinks,
            stdoutInteractive = stdoutInteractive,
            stdinInteractive = stdinInteractive
        )
    }

    fun detectSize(timeoutMs: Long): Pair<Int, Int>? {
        val process = try {
            ProcessBuilder("stty", "size")
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .start()
        } catch (e: IOException) {
            return null
        }
        try {
            if (!process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)) {
                return null
            }
        } catch (e: InterruptedException) {
            return null
        }

        val output = process.inputStream.bufferedReader().readText()
        val dimens = output.trim().split(" ").mapNotNull { it.toIntOrNull() }
        if (dimens.size != 2) return null
        return dimens[1] to dimens[0]
    }

    private fun width(): Int? = System.getenv("COLUMNS")?.toInt()
    private fun height(): Int? = System.getenv("LINES")?.toInt()

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
                System.getProperty("os.name") == "Windows 10" -> TRUECOLOR
                else -> ANSI256
            }
            "xterm", "vt100", "vt220", "screen", "color", "linux", "ansi", "rxvt", "konsole" -> ANSI16
            "dumb" -> NONE
            else -> NONE
        }
    }

    private fun getTerm() = System.getenv("TERM")?.toLowerCase()

    // https://github.com/termstandard/colors/
    private fun getColorTerm() = System.getenv("COLORTERM")?.toLowerCase()

    private fun forcedColor(): AnsiLevel? {
        return when {
            getTerm() == "dumb" -> NONE
            // https://no-color.org/
            System.getenv("NO_COLOR") != null -> NONE
            // A lot of npm packages support the FORCE_COLOR envvar, although they all look for
            // different values. We try to support them all.
            else -> when (System.getenv("FORCE_COLOR")?.toLowerCase()) {
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

    // Unfortunately, the JVM doesn't let us check stdin and stdout separately.
    private fun stdoutInteractive(): Boolean = System.console() != null
    private fun stdinInteractive(): Boolean = System.console() != null

    private fun isIntellijConsole(): Boolean {
        return hasIdeaEnvvar() || usingIdeaJavaAgent()
    }

    // Some versions of IntelliJ set various environment variables
    private fun hasIdeaEnvvar(): Boolean {
        return System.getenv("IDEA_INITIAL_DIRECTORY") != null
                || System.getenv("__INTELLIJ_COMMAND_HISTFILE__") != null
                || System.getenv("TERMINAL_EMULATOR")?.contains("jetbrains", ignoreCase = true) == true
                || System.getProperty("sun.java.command", "").contains("idea", ignoreCase = true)
    }

    // Depending on how IntelliJ is configured, it might use its own Java agent
    private fun usingIdeaJavaAgent() = try {
        val bean = ManagementFactory.getRuntimeMXBean()
        val jvmArgs = bean.inputArguments
        jvmArgs.any { it.startsWith("-javaagent") && "idea_rt.jar" in it }
    } catch (e: SecurityException) {
        false
    }
}
