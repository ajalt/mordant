package com.github.ajalt.mordant.animation.progress

import com.github.ajalt.mordant.animation.textAnimation
import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.widgets.progress.MultiProgressBarWidgetMaker
import com.github.ajalt.mordant.widgets.progress.ProgressBarMakerRow
import com.github.ajalt.mordant.widgets.progress.ProgressBarWidgetMaker
import com.github.ajalt.mordant.widgets.progress.completed
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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
        vt.output() shouldBe "${HIDE_CURSOR}1\r2\r3$SHOW_CURSOR"
        vt.clearOutput()
        a.stop()
        vt.output() shouldBe "\n$SHOW_CURSOR"
        vt.clearOutput()
    }

    @Test
    fun `interrupted animator shows cursor`() {
        val a = t.textAnimation<Unit> { "1" }.animateOnThread(fps = 1)
        Thread.currentThread().interrupt()
        shouldThrow<InterruptedException> { a.runBlocking() }
        vt.output() shouldBe "${HIDE_CURSOR}1$SHOW_CURSOR"
    }

    @Test
    fun `multi progress animator`() {
        val layout = progressBarLayout { completed(fps = 100) }
        val animation = MultiProgressBarAnimation(t).animateOnThread()
        val task1 = animation.addTask(layout, total = 10)
        val task2 = animation.addTask(layout, total = 10)
        task1.advance(10)
        task2.advance(10)
        animation.runBlocking()
        vt.output().shouldContain(" 10/10\n       10/10")
    }

    @Test
    fun `task finishing during refresh`() {
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
        val animation = MultiProgressBarAnimation(t, maker = maker)
        maker.task = animation.addTask(layout, Unit, total = 10)
        animation.animateOnThread().runBlocking()
        vt.output() shouldBe "$HIDE_CURSOR        0/10\r       10/10\n$SHOW_CURSOR"
    }
}

