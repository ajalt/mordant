package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.getTerminalSize
import com.github.ajalt.mordant.internal.isWindows
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.util.concurrent.TimeUnit
import kotlin.test.Test

class TerminalDetectionTest {
    @Test
    fun testDetectWidth() {
        if( isWindows()) return
        val cmd = ProcessBuilder("/usr/bin/env", "stty", "size")
        val proc =    cmd.redirectInput(ProcessBuilder.Redirect.INHERIT)
            .start()
        proc.waitFor(5, TimeUnit.SECONDS)
        val e = proc.errorStream.bufferedReader().readText()
        val o = proc.inputStream.bufferedReader().readText()
        """
        ---e
            $e
        ---
            $o
        ---
        """ shouldBe ""
    }
}
