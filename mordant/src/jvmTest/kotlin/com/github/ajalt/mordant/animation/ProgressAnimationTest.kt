package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.test.RenderingTest
import io.kotest.matchers.shouldBe
import java.util.concurrent.TimeUnit
import kotlin.test.Test

class ProgressAnimationTest : RenderingTest() {
    private var now = 0.0

    @Test
    fun throttling() {
        val vt = TerminalRecorder()
        val t = Terminal(terminalInterface = vt)
        val pt = t.progressAnimation {
            timeSource = { (now * TimeUnit.SECONDS.toNanos(1)).toLong() }
            textFrameRate = 1
            padding = 0
            speed()
            text("|")
            timeRemaining()
        }

        pt.update(0, 1000)
        now = 0.5
        vt.clearOutput()
        pt.update(40)
        vt.normalizedBuffer() shouldBe " ---.-it/s|eta -:--:--"

        now = 0.6
        vt.clearOutput()
        pt.update()
        vt.normalizedBuffer() shouldBe " ---.-it/s|eta -:--:--"

        now = 1.0
        vt.clearOutput()
        pt.update()
        vt.normalizedBuffer() shouldBe "  40.0it/s|eta 0:00:24"

        now = 1.9
        vt.clearOutput()
        pt.update()
        vt.normalizedBuffer() shouldBe "  40.0it/s|eta 0:00:24"
    }

    @Test
    fun animation() {
        val vt = TerminalRecorder(width = 56)
        val t = Terminal(
            theme = Theme(Theme.PlainAscii) { strings["progressbar.pending"] = "." },
            terminalInterface = vt
        )
        val pt = t.progressAnimation {
            timeSource = { (now * TimeUnit.SECONDS.toNanos(1)).toLong() }
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
        vt.normalizedBuffer() shouldBe "text.txt|  0%|......|   0.0/100.0| ---.-it/s|eta -:--:--"

        now = 10.0
        vt.clearOutput()
        pt.update(40)
        vt.normalizedBuffer() shouldBe "text.txt| 40%|##>...|  40.0/100.0|   4.0it/s|eta 0:00:15"

        now = 20.0
        vt.clearOutput()
        pt.update()
        vt.normalizedBuffer() shouldBe "text.txt| 40%|##>...|  40.0/100.0|   2.0it/s|eta 0:00:30"

        now = 30.0
        vt.clearOutput()
        pt.updateTotal(200)
        vt.normalizedBuffer() shouldBe "text.txt| 20%|#>....|  40.0/200.0|   1.3it/s|eta 0:02:00"

        vt.clearOutput()
        pt.restart()
        vt.normalizedBuffer() shouldBe "text.txt|  0%|......|   0.0/200.0| ---.-it/s|eta -:--:--"

        vt.clearOutput()
        pt.clear()
        vt.normalizedBuffer() shouldBe ""
    }

    private fun TerminalRecorder.normalizedBuffer(): String {
        return output().substringAfter("${CSI}0J").substringAfter("${CSI}1A").trimEnd()
    }
}
