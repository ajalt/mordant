package com.github.ajalt.mordant.animation.progress

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.test.latestOutput
import com.github.ajalt.mordant.test.normalizedOutput
import com.github.ajalt.mordant.test.visibleCrLf
import com.github.ajalt.mordant.widgets.progress.*
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class BaseProgressAnimationTest : RenderingTest() {
    private val now = TestTimeSource()
    private val vt = TerminalRecorder(width = 54)
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
        val a = MultiProgressBarAnimation(t, timeSource = now)
        val pt = a.addTask(l, total = 1000)

        a.refresh()
        vt.latestOutput() shouldBe " ---.-/s|eta -:--:--"

        now += 0.5.seconds
        vt.clearOutput()
        pt.update(40)
        a.refresh()
        vt.latestOutput() shouldBe " ---.-/s|eta -:--:--"

        now += 0.1.seconds
        vt.clearOutput()
        a.refresh()
        vt.latestOutput() shouldBe " ---.-/s|eta -:--:--"

        now += 0.4.seconds
        vt.clearOutput()
        a.refresh()
        vt.latestOutput() shouldBe "  40.0/s|eta 0:00:24"

        now += 0.9.seconds
        vt.clearOutput()
        a.refresh()
        vt.latestOutput() shouldBe "  40.0/s|eta 0:00:24"
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
        val a = MultiProgressBarAnimation(t, false, 1.minutes, timeSource = now)
        val pt = a.addTask(l, total = 100)

        a.refresh()
        vt.latestOutput() shouldBe "text.txt|  0%|......|       0/100| ---.-/s|eta -:--:--"

        now += 10.0.seconds
        vt.clearOutput()
        pt.update(40)
        a.refresh()
        vt.latestOutput() shouldBe "text.txt| 40%|##>...|      40/100|   4.0/s|eta 0:00:15"

        now += 10.0.seconds
        vt.clearOutput()
        a.refresh()
        vt.latestOutput() shouldBe "text.txt| 40%|##>...|      40/100|   2.0/s|eta 0:00:30"

        now += 10.0.seconds
        vt.clearOutput()
        pt.update { total = 200 }
        a.refresh()
        vt.latestOutput() shouldBe "text.txt| 20%|#>....|      40/200|   1.3/s|eta 0:02:00"

        vt.clearOutput()
        pt.reset()
        a.refresh()
        vt.latestOutput() shouldBe "text.txt|  0%|......|       0/200| ---.-/s|eta -:--:--"
    }

    @[Test JsName("task_visibility")]
    fun `task visibility`() {
        val l = progressBarContextLayout<Int>(textFps = 1, animationFps = 1) {
            text { "Task $context" }
        }
        val a = MultiProgressBarAnimation(t, timeSource = now)
        val t1 = a.addTask(l, 1)
        val t2 = a.addTask(l, 2)

        a.refresh()
        vt.latestOutput() shouldBe "Task 1\nTask 2"

        vt.clearOutput()
        t1.update { visible = false }
        a.refresh()
        vt.latestOutput() shouldBe "Task 2"

        vt.clearOutput()
        t2.update { visible = false }
        a.refresh()
        vt.latestOutput() shouldBe ""

        vt.clearOutput()
        t1.update { visible = true }
        a.refresh()
        vt.latestOutput() shouldBe "Task 1"

        vt.clearOutput()
        a.removeTask(t1)
        a.refresh()
        vt.latestOutput() shouldBe ""
    }

    @[Test JsName("changing_text_cell")]
    fun `changing text cell`() {
        val l = progressBarContextLayout(
            textFps = 1, animationFps = 1, alignColumns = false
        ) {
            text(align = TextAlign.LEFT) { context }
        }
        val a = MultiProgressBarAnimation(t, timeSource = now)
        a.addTask(l, "====")
        val t1 = a.addTask(l, "1111")

        a.refresh()
        t1.update { context = "22" }
        now += 1.seconds
        a.refresh()
        val moves = t.cursor.getMoves { startOfLine(); up(1) }
        vt.normalizedOutput().visibleCrLf() shouldBe "====\n1111${moves}====\n22  ".visibleCrLf()
    }

    @[Test JsName("different_context_types")]
    fun `different context types`() {
        val l1 = progressBarContextLayout<Int> { text { "int: $context" } }
        val l2 = progressBarContextLayout<String> { text { "string: $context" } }
        val l3 = progressBarLayout { text { "no context" } }
        val a = MultiProgressBarAnimation(t, timeSource = now)
        a.addTask(l1, 11)
        a.addTask(l2, "ss")
        a.addTask(l3)
        a.refresh()
        vt.output() shouldBe """
               int: 11
            string: ss
            no context
            """.trimIndent()
    }
}
