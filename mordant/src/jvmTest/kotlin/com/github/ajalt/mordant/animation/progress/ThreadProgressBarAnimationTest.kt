package com.github.ajalt.mordant.animation.progress

import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.widgets.progress.completed
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import io.kotest.matchers.shouldBe
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test

private const val ESC = "\u001B"
private const val CSI = "$ESC["
private const val SHOW_CURSOR = "$CSI?25h"

class ThreadProgressBarAnimationTest {
    private val vt = TerminalRecorder(width = 56)
    private val t = Terminal(terminalInterface = vt)

    @Test
    fun `smoke test`()  {
        val a = progressBarLayout(spacing = 0) {
            completed(fps=100)
        }.animateOnThread(t)
        val t = a.addTask(total = 10)
        val service = Executors.newSingleThreadExecutor()
        try {
            var future = a.execute(service)

            t.update(5)
            Thread.sleep(50)
            vt.clearOutput()
            Thread.sleep(50)
            vt.normalizedOutput() shouldBe "        5/10"

            Thread.sleep(50)
            future.isDone shouldBe false

            t.update(10)
            future.get(100, TimeUnit.MILLISECONDS)
            future.isDone shouldBe true

            t.reset()
            future = a.execute(service)
            Thread.sleep(50)
            vt.clearOutput()
            a.stop()
            future.get(100, TimeUnit.MILLISECONDS)
            vt.normalizedOutput() shouldBe "        0/10\n$SHOW_CURSOR"

            t.reset()
            future = a.execute(service)
            Thread.sleep(50)
            vt.clearOutput()
            a.clear()
            future.get(100, TimeUnit.MILLISECONDS)
            vt.normalizedOutput() shouldBe "${CSI}0J$SHOW_CURSOR"
        } finally {
            service.shutdownNow()
        }
    }

    private fun TerminalRecorder.normalizedOutput(): String {
        return output().substringAfterLast("${CSI}1A").trimEnd()
    }
}

