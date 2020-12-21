package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.VirtualTerminalInterface
import io.kotest.matchers.shouldBe
import org.junit.Test

class ProgressTrackerTest {
    @Test
    fun allCells() {
        val vt = VirtualTerminalInterface(width = 56)
        val t = Terminal(
            theme = Theme(Theme.PlainAscii) { strings["progressbar.pending"] = "." },
            terminalInterface = vt
        )
        val pt = t.progressTracker {
            padding = 0
            autoUpdate = false
            text("text.txt")
            text("|")
            percentage()
            text("|")
            progressBar()
            text("|")
            completed()
            text("|")
            speed(frameRate = null)
            text("|")
            timeRemaining(frameRate = null)
        }
        pt.update(0, 100)
        pt.update(10)
        pt.update(20)
        pt.update(30)
        vt.clearBuffer()
        pt.update(40)
        vt.normalizedBuffer() shouldBe "text.txt| 40%|###>....| 40.0/100.0B|123.4B/s|eta 0:00:01"

        vt.clearBuffer()
        pt.updateTotal(200)
        vt.normalizedBuffer() shouldBe "text.txt| 20%|#>......| 40.0/200.0B|123.4B/s|eta 0:00:01"

        vt.clearBuffer()
        pt.restart()
        vt.normalizedBuffer() shouldBe "text.txt|  0%|........|  0.0/200.0B|123.4B/s|eta -:--:--"

        vt.clearBuffer()
        pt.clear()
        vt.normalizedBuffer() shouldBe ""
    }

    private fun VirtualTerminalInterface.normalizedBuffer(): String {
        return buffer().substringAfter("${CSI}0J").trimEnd().replace(Regex("\\w+\\.\\wB/s"), "123.4B/s")
    }
}
