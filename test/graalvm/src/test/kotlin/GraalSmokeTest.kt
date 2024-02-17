package com.github.ajalt.mordant.graalvm

import com.github.ajalt.mordant.animation.progress.animateOnThread
import com.github.ajalt.mordant.animation.progress.execute
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.widgets.progress.progressBar
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class GraalSmokeTest {
    @Test
    fun `progress animation test`() {
        // Just make sure it doesn't crash, exact output is verified in the normal test suite
        val t = Terminal(interactive = true, ansiLevel = AnsiLevel.TRUECOLOR)
        val animation = progressBarLayout { progressBar() }.animateOnThread(t, total = 1)
        val future = animation.execute()
        Thread.sleep(100)
        animation.update { completed = 1 }
        future.get(100, TimeUnit.MILLISECONDS)
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
