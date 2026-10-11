package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.CSI
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.Test

class JvmTerminalCursorTest {
    @Test
    fun `hide leaves the cursor visible when nothing would show it again`() = forAll(
        // Registering the hook fails, so hiding would outlast the JVM
        row("unregistered", ""),
        // The hook has already shown the cursor, and won't run again
        row("hook-ran", "$CSI?25l$CSI?25h$CSI?25h"),
    ) { mode, expected ->
        val output = File.createTempFile("mordant-cursor-shutdown", ".txt")
        try {
            val java = File(System.getProperty("java.home"), "bin/java").let {
                if (it.canExecute()) it else File(it.path + ".exe")
            }
            val process = ProcessBuilder(
                java.path, "-cp", System.getProperty("java.class.path"),
                CursorShutdownProgram::class.java.name, output.path, mode,
            ).start()
            process.waitFor(60, TimeUnit.SECONDS) shouldBe true
            output.readText() shouldBe expected
        } finally {
            output.delete()
        }
    }
}
