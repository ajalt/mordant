package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.test.RenderingTest
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.contrib.java.lang.system.SystemErrRule
import org.junit.contrib.java.lang.system.SystemOutRule
import kotlin.test.Test

class StderrTerminalTest : RenderingTest() {
    @get:Rule
    val stdout: SystemOutRule = SystemOutRule().enableLog().muteForSuccessfulTests()

    @get:Rule
    val stderr: SystemErrRule = SystemErrRule().enableLog().muteForSuccessfulTests()

    @Test
    fun stderrTerminal() {
        val terminal = Terminal()

        terminal.print("foo", stderr = true)
        terminal.print("bar")

        stderr.logWithNormalizedLineSeparator shouldBe "foo"
        stdout.logWithNormalizedLineSeparator shouldBe "bar"
    }
}
