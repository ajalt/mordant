package com.github.ajalt.mordant.animation.progress

import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.widgets.progress.*
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
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
        val a = BaseProgressBarAnimation(t, l.cache(now))
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
        val a = BaseProgressBarAnimation(t, l.cache(now), 1.minutes)
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
        a.visible = false
        a.refresh()
        vt.normalizedOutput() shouldBe ""
    }

    @Test
    @JsName("task_visibility")
    fun `task visibility`() {
        val l = progressBarContextLayout<Int>(textFps = 1, animationFps = 1) {
            text { "Task $context" }
        }
        val a = BaseProgressBarAnimation(t, l.cache(now))
        val t1 = a.addTask(1)
        val t2 = a.addTask(2)

        a.refresh()
        vt.normalizedOutput() shouldBe "Task 1\nTask 2"

        vt.clearOutput()
        t1.update { visible = false }
        a.refresh()
        vt.normalizedOutput() shouldBe "Task 2"

        vt.clearOutput()
        t2.update { visible = false }
        a.refresh()
        vt.normalizedOutput() shouldBe ""

        vt.clearOutput()
        t1.update { visible = true }
        a.refresh()
        vt.normalizedOutput() shouldBe "Task 1"

        vt.clearOutput()
        t2.update { visible = true }
        a.visible = false
        a.refresh()
        vt.normalizedOutput() shouldBe ""

        vt.clearOutput()
        a.visible = true
        a.refresh()
        vt.normalizedOutput() shouldBe "Task 1\nTask 2"
    }

    @Test
    @JsName("changing_text_cell")
    fun `changing text cell`() {
        val l = progressBarContextLayout<Int>(textFps = 1, animationFps = 1) {
            text { "Task $context" }
        }
        val a = BaseProgressBarAnimation(t, l.cache(now))
        val t1 = a.addTask(1)

        a.refresh()
        vt.normalizedOutput() shouldBe "Task 1"

        vt.clearOutput()
        t1.update { context = 2 }
        now += 1.seconds
        a.refresh()
        vt.normalizedOutput() shouldBe "Task 2"
    }

    private fun TerminalRecorder.normalizedOutput(): String {
        return output().substringAfter("${CSI}0J").substringAfter("${CSI}1A").trimStart('\r')
            .trimEnd()
    }
}
