package com.github.ajalt.mordant.animation.progress

import com.github.ajalt.mordant.animation.textAnimation
import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.widgets.progress.completed
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test

private const val HIDE_CURSOR = "$CSI?25l"
private const val SHOW_CURSOR = "$CSI?25h"

class ThreadAnimatorTest {
    private val vt = TerminalRecorder(width = 56)
    private val t = Terminal(terminalInterface = vt)

    @Test
    fun `unit animator`() {
        var i = 1
        val a = t.textAnimation<Unit> { "${i++}" }.animateOnThread(fps = 10000) { i > 2 }
        a.runBlocking()
        vt.output() shouldBe "${HIDE_CURSOR}1\r2\r3"
        vt.clearOutput()
        a.stop()
        vt.output() shouldBe "\n$SHOW_CURSOR"
        vt.clearOutput()
    }

    @Test
    fun `multi progress animator`() {
        val layout = progressBarLayout { completed(fps = 100) }
        val animation = MultiProgressBarAnimation<Unit>(t).animateOnThread(t)
        val task1 = animation.addTask(layout, total = 10)
        val task2 = animation.addTask(layout, total = 10)
        task1.advance(10)
        task2.advance(10)
        animation.runBlocking()
        vt.output().shouldContain(" 10/10\n       10/10")
    }

    @Test
    fun `smoke test`() {
        val a = progressBarLayout(spacing = 0) {
            completed(fps = 100)
        }.animateOnThread(t, total = 10)
        val service = Executors.newSingleThreadExecutor()
        try {
            var future = a.execute(service)

            a.update(5)
            Thread.sleep(20)
            future.isDone shouldBe false
            a.update(10)
            future.get(100, TimeUnit.MILLISECONDS)
            vt.output().shouldContain(" 10/10")

            vt.clearOutput()
            a.reset()
            future = a.execute(service)
            Thread.sleep(20)
            a.stop()
            future.get(100, TimeUnit.MILLISECONDS)
            vt.output().shouldContain(" 0/10")

            vt.clearOutput()
            a.reset()
            future = a.execute(service)
            Thread.sleep(20)
            a.clear()
            future.get(100, TimeUnit.MILLISECONDS)
        } finally {
            service.shutdownNow()
        }
    }
}

