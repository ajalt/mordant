package com.github.ajalt.mordant.animation.progress

import com.github.ajalt.mordant.animation.textAnimation
import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.widgets.progress.completed
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import java.io.File
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
        vt.output() shouldBe "${HIDE_CURSOR}1\r2\r3$SHOW_CURSOR"
        vt.clearOutput()
        a.stop()
        vt.output() shouldBe "\n$SHOW_CURSOR"
        vt.clearOutput()
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
    fun `runBlocking shows the cursor when the animation finishes on its own`() {
        val layout = progressBarLayout { completed(fps = 100) }
        val animation = MultiProgressBarAnimation(t).animateOnThread()
        animation.addTask(layout, total = 10).advance(10)
        animation.runBlocking()
        vt.output() shouldEndWith SHOW_CURSOR
    }

    @Test
    fun `execute stops drawing once the jvm starts shutting down`() {
        val output = File.createTempFile("mordant-animator-shutdown", ".txt")
        try {
            val java = File(System.getProperty("java.home"), "bin/java").let {
                if (it.canExecute()) it else File(it.path + ".exe")
            }
            val process = ProcessBuilder(
                java.path, "-cp", System.getProperty("java.class.path"),
                AnimatorShutdownProgram::class.java.name, output.path,
            ).start()
            process.waitFor(60, TimeUnit.SECONDS) shouldBe true

            // The animator's thread is a daemon, so shutdown doesn't stop it. Both exit hooks end
            // by showing the cursor, so with the animation stopped the last write is always one of
            // them; without it the daemon keeps painting frames until the JVM halts.
            val text = output.readText()
            text shouldContain HIDE_CURSOR
            text shouldEndWith SHOW_CURSOR
        } finally {
            output.delete()
        }
    }
}

