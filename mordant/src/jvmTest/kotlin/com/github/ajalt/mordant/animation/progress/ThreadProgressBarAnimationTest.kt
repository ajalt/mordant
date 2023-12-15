package com.github.ajalt.mordant.animation.progress

import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.widgets.progress.completed
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import io.kotest.matchers.shouldBe
import java.util.concurrent.Executors
import kotlin.test.Test


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
            val future = a.execute(service)

            t.update(5)
            Thread.sleep(20)
            vt.clearOutput()
            Thread.sleep(20)
            vt.normalizedOutput() shouldBe "    5.0/10.0"

            Thread.sleep(20)
            future.isDone shouldBe false

            t.update(10)
            Thread.sleep(20)
            future.isDone shouldBe true
        } finally {
            service.shutdownNow()
        }
    }

    private fun TerminalRecorder.normalizedOutput(): String {
        return output().substringAfterLast("${CSI}1A").trimEnd()
    }
}

