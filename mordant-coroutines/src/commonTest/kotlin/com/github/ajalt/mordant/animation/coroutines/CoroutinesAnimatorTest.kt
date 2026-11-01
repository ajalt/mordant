package com.github.ajalt.mordant.animation.coroutines

import com.github.ajalt.mordant.animation.progress.MultiProgressBarAnimation
import com.github.ajalt.mordant.animation.progress.ProgressTask
import com.github.ajalt.mordant.animation.progress.addTask
import com.github.ajalt.mordant.animation.progress.advance
import com.github.ajalt.mordant.animation.progress.update
import com.github.ajalt.mordant.animation.textAnimation
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.widgets.progress.MultiProgressBarWidgetMaker
import com.github.ajalt.mordant.widgets.progress.ProgressBarMakerRow
import com.github.ajalt.mordant.widgets.progress.ProgressBarWidgetMaker
import com.github.ajalt.mordant.widgets.progress.completed
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

private const val ESC = "\u001B"
private const val CSI = "$ESC["
private const val HIDE_CURSOR = "$CSI?25l"
private const val SHOW_CURSOR = "$CSI?25h"

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class CoroutinesAnimatorTest {
    private val vt = TerminalRecorder(width = 56)
    private val t = Terminal(terminalInterface = vt)

    @Test
    fun testFps() = runTest {
        val a = progressBarLayout(spacing = 0, textFps = 1) {
            completed(suffix = "a")
            completed(suffix = "b", fps = 2)
        }.animateInCoroutine(t, total = 10, timeSource = testTimeSource)

        val job = backgroundScope.launch { a.execute() }

        advanceTimeBy(0.1.seconds)
        vt.normalizedOutput() shouldBe "$HIDE_CURSOR        0/10a        0/10b"
        vt.clearOutput()

        a.update(5)
        advanceTimeBy(0.1.seconds)
        vt.output() shouldBe ""

        advanceTimeBy(0.4.seconds)
        vt.normalizedOutput() shouldBe "        0/10a        5/10b"

        vt.clearOutput()
        advanceTimeBy(0.5.seconds)
        vt.normalizedOutput() shouldBe "        5/10a        5/10b"

        advanceTimeBy(10.seconds)

        job.isActive shouldBe true
        a.update(10)

        advanceTimeBy(1.seconds)
        job.isActive shouldBe false
    }

   @[Test JsName("stop_and_clear")]
    fun `stop and clear`() = runTest {
        val a = progressBarLayout(spacing = 0, textFps = 1) {
            completed()
        }.animateInCoroutine(t, total = 10, timeSource = testTimeSource)
        var job = backgroundScope.launch { a.execute() }
        advanceTimeBy(0.1.seconds)
        a.stop()
        advanceTimeBy(1.0.seconds)
        job.isActive shouldBe false
        vt.fullNormalizedOutput() shouldBe "$HIDE_CURSOR        0/10\n$SHOW_CURSOR"

        vt.clearOutput()
        job = backgroundScope.launch { a.execute() }
        advanceTimeBy(0.1.seconds)
        a.clear()
        advanceTimeBy(1.0.seconds)
        job.isActive shouldBe false
        vt.fullNormalizedOutput() shouldBe "$HIDE_CURSOR        0/10\r${CSI}0J$SHOW_CURSOR"
    }

   @[Test JsName("unit_animation")]
    fun `unit animation`() = runTest {
        var i = 1
        var fin = false
        val a = t.textAnimation<Unit> { "$i" }.animateInCoroutine(fps = 1) { fin }
        val job = backgroundScope.launch { a.execute() }
        advanceTimeBy(0.1.seconds)
        vt.normalizedOutput() shouldBe "$CSI?25l1" // hide cursor
        vt.clearOutput()

        i = 2
        advanceTimeBy(0.1.seconds)
        vt.output() shouldBe ""

        advanceTimeBy(1.0.seconds)
        vt.normalizedOutput() shouldBe "2"

        job.isActive shouldBe true
        fin = true

        advanceTimeBy(1.seconds)
        job.isActive shouldBe false
        vt.output() shouldEndWith SHOW_CURSOR
    }

    @[Test JsName("cancelled_animation_shows_cursor")]
    fun `cancelled animation shows cursor`() = runTest {
        val a = t.textAnimation<Unit> { "1" }.animateInCoroutine(fps = 1)
        val job = backgroundScope.launch { a.execute() }
        advanceTimeBy(0.1.seconds)
        job.cancelAndJoin()
        vt.output() shouldBe "${HIDE_CURSOR}1$SHOW_CURSOR"
    }

   @[Test JsName("multi_progress_animation")]
    fun `multi progress animation`() = runTest {
        val layout = progressBarLayout { completed(fps = 1) }
        val animation = MultiProgressBarAnimation(t).animateInCoroutine()
        val task1 = animation.addTask(layout, total = 10)
        val task2 = animation.addTask(layout, total = 10)
        backgroundScope.launch { animation.execute() }
        task1.advance(10)
        task2.advance(10)
        advanceTimeBy(1.1.seconds)
        vt.output().shouldContain(" 10/10\n       10/10")
    }

   @[Test JsName("task_finishing_during_refresh")]
    fun `task finishing during refresh`() = runTest {
        val maker = object : ProgressBarWidgetMaker {
            var task: ProgressTask<Unit>? = null
            private var fired = false
            override fun build(rows: List<ProgressBarMakerRow<*>>): Widget {
                if (!fired) {
                    fired = true
                    task?.advance(10)
                }
                return MultiProgressBarWidgetMaker.build(rows)
            }
        }
        val layout = progressBarLayout(spacing = 0, textFps = 1, animationFps = 1) { completed() }
        val animation = MultiProgressBarAnimation(t, maker = maker, timeSource = testTimeSource)
        maker.task = animation.addTask(layout, Unit, total = 10)
        val job = backgroundScope.launch { animation.animateInCoroutine().execute() }
        advanceTimeBy(1.1.seconds)
        job.isActive shouldBe false
        vt.fullNormalizedOutput() shouldBe "$HIDE_CURSOR        0/10\r       10/10\n$SHOW_CURSOR"
    }

    // This handles the difference in wasm movements and the other targets
    private fun TerminalRecorder.fullNormalizedOutput(): String {
        return output().replace("${CSI}1A", "")
    }

    private fun TerminalRecorder.normalizedOutput(): String {
        return output().replace("${CSI}1A", "").substringAfter("\r").trimEnd()
    }
}
