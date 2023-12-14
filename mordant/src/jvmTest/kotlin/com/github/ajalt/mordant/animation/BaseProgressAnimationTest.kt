package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.widgets.progress.*
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class BaseProgressAnimationTest : RenderingTest() {
    private val now = TestTimeSource()
    private val vt = TerminalRecorder(width = 56)
    private val t = Terminal(
        theme = Theme(Theme.PlainAscii) { strings["progressbar.pending"] = "." },
        terminalInterface = vt
    )

    @Test
    fun throttling() {
        val l = progressBarLayout(spacing = 0, textFps = 1) {
            speed()
            text("|")
            timeRemaining(fps = 1)
        }
        val a = BaseProgressBarAnimation(t, l.cache(now), now)
        val pt = a.addTask(total = 1000)

        a.refresh()
        vt.normalizedOutput() shouldBe " ---.-it/s|eta -:--:--"

        now += 0.5.seconds
        vt.clearOutput()
        pt.update(40)
        a.refresh()
        vt.normalizedOutput() shouldBe " ---.-it/s|eta -:--:--"

        now += 0.1.seconds
        vt.clearOutput()
        a.refresh()
        vt.normalizedOutput() shouldBe " ---.-it/s|eta -:--:--"

        now += 0.4.seconds
        vt.clearOutput()
        a.refresh()
        vt.normalizedOutput() shouldBe "  40.0it/s|eta 0:00:24"

        now += 0.9.seconds
        vt.clearOutput()
        a.refresh()
        vt.normalizedOutput() shouldBe "  40.0it/s|eta 0:00:24"
    }

    @Test
    fun animation() {
        val l = progressBarLayout(spacing = 0, textFps = 1, animationFps = 1) {
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
        val a = BaseProgressBarAnimation(t, l.cache(now), now, 1.minutes)
        val pt = a.addTask(total = 100)

        a.refresh()
        vt.normalizedOutput() shouldBe "text.txt|  0%|......|   0.0/100.0| ---.-it/s|eta -:--:--"

        now += 10.0.seconds
        vt.clearOutput()
        pt.update(40)
        a.refresh()
        vt.normalizedOutput() shouldBe "text.txt| 40%|##>...|  40.0/100.0|   4.0it/s|eta 0:00:15"

        now += 10.0.seconds
        vt.clearOutput()
        a.refresh()
        vt.normalizedOutput() shouldBe "text.txt| 40%|##>...|  40.0/100.0|   2.0it/s|eta 0:00:30"

        now += 10.0.seconds
        vt.clearOutput()
        pt.update { total = 200 }
        a.refresh()
        vt.normalizedOutput() shouldBe "text.txt| 20%|#>....|  40.0/200.0|   1.3it/s|eta 0:02:00"

        vt.clearOutput()
        pt.reset()
        a.refresh()
        vt.normalizedOutput() shouldBe "text.txt|  0%|......|   0.0/200.0| ---.-it/s|eta -:--:--"

        vt.clearOutput()
        a.clear()
        vt.normalizedOutput() shouldBe ""
    }

    private fun TerminalRecorder.normalizedOutput(): String {
        return output().substringAfter("${CSI}0J").substringAfter("${CSI}1A").trimEnd()
    }
}
