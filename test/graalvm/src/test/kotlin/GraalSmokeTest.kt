package com.github.ajalt.mordant.graalvm

import com.github.ajalt.mordant.animation.progressAnimation
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GraalSmokeTest {
    @Test
    fun `progress animation test`() = runTest {
        // Just make sure it doesn't crash, exact output is verified in the normal test suite
        val t = Terminal(interactive = true, ansiLevel = AnsiLevel.TRUECOLOR)
        val animation = t.progressAnimation { progressBar() }
        animation.start()
        Thread.sleep(100)
        animation.clear()
    }

    @Test
    fun `terminal detection test`() {
        // Just make sure that the terminal detection doesn't crash.
        Terminal()
    }

    @Test
    fun `markdown test`() {
        val vt = TerminalRecorder()
        val t = Terminal(terminalInterface = vt)
        t.print(Markdown("- Some **bold** text"))
        assertEquals(" • Some ${bold("bold")} text", vt.output())
    }
}
