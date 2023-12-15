package com.github.ajalt.mordant.animation.coroutines

import com.github.ajalt.mordant.animation.addTask
import com.github.ajalt.mordant.animation.update
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.widgets.progress.completed
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

private const val ESC = "\u001B"
private const val CSI = "$ESC["

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

    private fun TerminalRecorder.normalizedOutput(): String {
        return output().substringAfter("${CSI}0J").substringAfter("${CSI}1A").trimEnd()
    }
}
