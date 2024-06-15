package com.github.ajalt.mordant.graalvm

import com.github.ajalt.mordant.animation.progress.animateOnThread
import com.github.ajalt.mordant.animation.progress.execute
import com.github.ajalt.mordant.input.enterRawMode
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.widgets.progress.progressBar
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.TimeUnit
import kotlin.test.Ignore
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * Smoke tests for the GraalVM platform.
 *
 * They just make sure nothing crashes; the actual output is verified in the normal test suite.
 */
class GraalSmokeTest {
    @Test
    fun `terminal detection test`() {
        Terminal()
    }

    @Test
    fun `progress animation test`() {
        val t = Terminal(interactive = true, ansiLevel = AnsiLevel.TRUECOLOR)
        val animation = progressBarLayout { progressBar() }.animateOnThread(t, total = 1)
        val future = animation.execute()
        Thread.sleep(100)
        animation.update { completed = 1 }
        future.get(1000, TimeUnit.MILLISECONDS)
    }

    @Ignore("Raw mode is currently unsupported on native-image")
    @Test
    fun `raw mode test`() {
        val t = Terminal(interactive = true)
        assertThrows<RuntimeException> {
            t.enterRawMode().use {}
        }
    }

    @Test
    fun `markdown test`() {
        val vt = TerminalRecorder()
        val t = Terminal(terminalInterface = vt)
        t.print(Markdown("- Some **bold** text"))
        assertEquals(" • Some ${bold("bold")} text", vt.output())
    }
}
