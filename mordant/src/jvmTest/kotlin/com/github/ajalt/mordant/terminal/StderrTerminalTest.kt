package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.test.RenderingTest
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.contrib.java.lang.system.SystemErrRule
import org.junit.contrib.java.lang.system.SystemOutRule
import kotlin.test.Test

class StderrTerminalTest : RenderingTest() {
    @Rule
    @JvmField
    val stdout = SystemOutRule().enableLog().muteForSuccessfulTests()

    @Rule
    @JvmField
    val stderr = SystemErrRule().enableLog().muteForSuccessfulTests()

    @Test
    fun stderrTerminal() {
        val outterm = Terminal()
        val errterm = Terminal().forStdErr()

        errterm.print("foo")
        outterm.print("bar")

        stderr.logWithNormalizedLineSeparator shouldBe "foo"
        stdout.logWithNormalizedLineSeparator shouldBe "bar"
    }
}
