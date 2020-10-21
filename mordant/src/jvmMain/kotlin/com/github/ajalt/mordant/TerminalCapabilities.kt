package com.github.ajalt.mordant

import com.github.ajalt.mordant.AnsiLevel.*
import java.lang.management.ManagementFactory

object TerminalCapabilities {
    /**
     * Return the best guess at the current terminal's support for ANSI colors.
     *
     * This will always return `NONE` if [consoleAvailable] returns false.
     *
     * @param default The value to return if support can't be detected.
     */
    fun detectANSISupport(default: AnsiLevel = NONE): AnsiLevel {
        // Consoles built in to some IDEs/Editors support color, but always cause System.console() to return null
        if (isIntellijConsole()) return ANSI16

        // https://github.com/Microsoft/vscode/pull/30346
        if (System.getenv("TERM_PROGRAM")?.toLowerCase() == "vscode") return ANSI256

        // https://github.com/microsoft/terminal/issues/1040#issuecomment-496691842
        if (!System.getenv("WT_SESSION").isNullOrEmpty()) return TRUECOLOR

        if (!consoleAvailable()) return NONE

        // https://github.com/termstandard/colors/
        when (System.getenv("COLORTERM")?.toLowerCase()) {
            "24bit", "24bits", "truecolor" -> return TRUECOLOR
        }

        when (System.getenv("TERM_PROGRAM")?.toLowerCase()) {
            "hyper" -> TRUECOLOR // stackoverflow.com/q/7052683
            "apple_terminal" -> ANSI256
            "iterm.app" -> {
                val ver = System.getenv("TERM_PROGRAM_VERSION")?.split(".")?.firstOrNull()?.toIntOrNull()
                return if (ver != null && ver >= 3) TRUECOLOR else ANSI256
            }
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
            else -> default
        }
    }

    // Although we really want to know if stdout is a tty regardless of stdin, this is the best the java gives
    // us. We might want to add some JNI code to do that in the future.
    /** Return true if stdout and stdin are both ttys. */
    fun consoleAvailable(): Boolean = System.console() != null

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
