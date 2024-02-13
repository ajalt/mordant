package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.test.normalizedOutput
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class DeprecatedProgressAnimationTest : RenderingTest() {
    private val now = TestTimeSource()
    private val vt = TerminalRecorder(width = 56)
    private val t = Terminal(
        theme = Theme(Theme.PlainAscii) { strings["progressbar.pending"] = "." },
        terminalInterface = vt
    )

    @Test
    fun throttling() {
        val pt = t.progressAnimation(now) {
            textFrameRate = 1
            padding = 0
            speed()
            text("|")
            timeRemaining()
        }

        pt.update(0, 1000)
        vt.normalizedOutput() shouldBe " ---.-it/s|eta -:--:--"

        now += 0.5.seconds
        vt.clearOutput()
        pt.update(40)
        vt.normalizedOutput() shouldBe " ---.-it/s|eta -:--:--"

        now += 0.1.seconds
        vt.clearOutput()
        pt.update()
        vt.normalizedOutput() shouldBe " ---.-it/s|eta -:--:--"

        now += 0.4.seconds
        vt.clearOutput()
        pt.update()
        vt.normalizedOutput() shouldBe "  40.0it/s|eta 0:00:24"

        now += 0.9.seconds
        vt.clearOutput()
        pt.update()
        vt.normalizedOutput() shouldBe "  40.0it/s|eta 0:00:24"
    }

    @Test
    fun animation() {
        val pt = t.progressAnimation(now) {
            textFrameRate = 1
            padding = 0
            text("text.txt")
            text("|")
            percentage()
            text("|")
            progressBar()
            text("|")
            completed()
            text("|")
            speed()
            text("|")
            timeRemaining()
        }
        pt.update(0, 100)
        vt.normalizedOutput() shouldBe "text.txt|  0%|......|       0/100| ---.-it/s|eta -:--:--"

        now += 10.0.seconds
        vt.clearOutput()
        pt.update(40)
        vt.normalizedOutput() shouldBe "text.txt| 40%|##>...|      40/100|   4.0it/s|eta 0:00:15"

        now += 10.0.seconds
        vt.clearOutput()
        pt.update()
        vt.normalizedOutput() shouldBe "text.txt| 40%|##>...|      40/100|   2.0it/s|eta 0:00:30"

        now += 10.0.seconds
        vt.clearOutput()
        pt.updateTotal(200)
        vt.normalizedOutput() shouldBe "text.txt| 20%|#>....|      40/200|   1.3it/s|eta 0:02:00"

        vt.clearOutput()
        pt.restart()
        vt.normalizedOutput() shouldBe "text.txt|  0%|......|       0/200| ---.-it/s|eta -:--:--"

        vt.clearOutput()
        pt.clear()
        val moves = t.cursor.getMoves { startOfLine(); clearScreenAfterCursor() }
        vt.output() shouldBe "$moves$CSI?25h"
    }
}
