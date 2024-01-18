package com.github.ajalt.mordant.animation.coroutines

import com.github.ajalt.mordant.animation.progress.addTask
import com.github.ajalt.mordant.animation.progress.update
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.widgets.progress.completed
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import com.github.ajalt.mordant.widgets.progress.text
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class CoroutinesProgressBarAnimationTest {
    private val vt = TerminalRecorder(width = 56)
    private val t = Terminal(terminalInterface = vt)

    @Test
    fun testFps() = runTest {
        val a = progressBarLayout(spacing = 0, textFps = 1) {
            completed(suffix = "a")
            completed(suffix = "b", fps = 2)
        }.animateInCoroutine(t, testTimeSource)
        val t = a.addTask(total = 10)

        val job = backgroundScope.launch { a.run() }

        advanceTimeBy(0.1.seconds)
        vt.normalizedOutput() shouldBe "$CSI?25l    0.0/10.0a    0.0/10.0b" // hide cursor
        vt.clearOutput()

        t.update(5)
        advanceTimeBy(0.1.seconds)
        vt.output() shouldBe ""

        advanceTimeBy(0.4.seconds)
        vt.normalizedOutput() shouldBe "    0.0/10.0a    5.0/10.0b"

        vt.clearOutput()
        advanceTimeBy(0.5.seconds)
        vt.normalizedOutput() shouldBe "    5.0/10.0a    5.0/10.0b"

        advanceTimeBy(10.seconds)

        job.isActive shouldBe true
        t.update(10)

        advanceTimeBy(1.seconds)
        job.isActive shouldBe false
    }

    @Test
    @JsName("test_stop_and_clear")
    fun `test stop and clear`() = runTest {
        val a = progressBarLayout(spacing = 0, textFps = 1) {
            completed()
        }.animateInCoroutine(t, testTimeSource)
        val t = a.addTask(total = 10)
        var job = backgroundScope.launch { a.run() }
        advanceTimeBy(0.1.seconds)
        a.stop()
        advanceTimeBy(1.0.seconds)
        job.isActive shouldBe false
        vt.output() shouldBe "$HIDE_CURSOR        0/10\n$SHOW_CURSOR"

        vt.clearOutput()
        job = backgroundScope.launch { a.run() }
        advanceTimeBy(0.1.seconds)
        a.clear()
        advanceTimeBy(1.0.seconds)
        job.isActive shouldBe false
        vt.output() shouldBe "$HIDE_CURSOR        0/10\n\r${CSI}1A${CSI}0J$SHOW_CURSOR"
    }

    private fun TerminalRecorder.normalizedOutput(): String {
        return output().substringAfter("${CSI}0J").substringAfter("${CSI}1A").trimEnd()
    }
}
