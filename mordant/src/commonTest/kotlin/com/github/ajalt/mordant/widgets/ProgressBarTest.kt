package com.github.ajalt.mordant.widgets

import com.github.ajalt.colormath.RGB
import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.rendering.RenderingTest
import com.github.ajalt.mordant.rendering.TextColors.gray
import com.github.ajalt.mordant.rendering.TextColors.magenta
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.Theme
import kotlin.test.Test

class ProgressBarTest : RenderingTest() {
    @Test
    fun `0 percent complete`() = doPercentTest(0, "     ")

    @Test
    fun `10 percent complete`() = doPercentTest(10, "     ")

    @Test
    fun `20 percent complete`() = doPercentTest(20, "#>   ")

    @Test
    fun `30 percent complete`() = doPercentTest(30, "#>   ")

    @Test
    fun `40 percent complete`() = doPercentTest(40, "##>  ")

    @Test
    fun `60 percent complete`() = doPercentTest(60, "###> ")

    @Test
    fun `80 percent complete`() = doPercentTest(80, "####>")

    @Test
    fun `99 percent complete`() = doPercentTest(99, "####>")

    @Test
    fun `100 percent complete`() = doPercentTest(100, "#####")

    @Test
    fun `default theme`() = doPercentTest(
        40,
        "${CSI}38;2;16;152;157m━━${CSI}39m ${CSI}38;2;64;81;78m━━${CSI}39m",
        theme = Theme.Default
    )

    @Test
    fun `pulse initial`() = doPulseTest(
        pulsePosition = 0f,
        "${CSI}38;2;16;152;157m━━━━━━━━━━${CSI}39m"
    )

    @Test
    fun `pulse 25`() = doPulseTest(
        pulsePosition = .25f,
        "${CSI}38;2;255;255;255m━${CSI}38;2;200;248;249m━${CSI}38;2;89;233;238m━${CSI}38;2;22;202;208m━${CSI}38;2;18;166;171m━${CSI}38;2;16;152;157m━━━━━${CSI}39m"
    )

    @Test
    fun `pulse 50`() = doPulseTest(
        pulsePosition = .50f,
        "${CSI}38;2;16;152;157m━${CSI}38;2;18;166;171m━${CSI}38;2;22;202;208m━${CSI}38;2;89;233;238m━${CSI}38;2;200;248;249m━${CSI}38;2;255;255;255m━${CSI}38;2;200;248;249m━${CSI}38;2;89;233;238m━${CSI}38;2;22;202;208m━${CSI}38;2;18;166;171m━${CSI}39m"
    )

    @Test
    fun `pulse 75`() = doPulseTest(
        pulsePosition = .75f,
        "${CSI}38;2;16;152;157m━━━━━━${CSI}38;2;18;166;171m━${CSI}38;2;22;202;208m━${CSI}38;2;89;233;238m━${CSI}38;2;200;248;249m━${CSI}39m"
    )

    @Test
    fun `pulse 100`() = doPulseTest(
        pulsePosition = 1f,
        "${CSI}38;2;16;152;157m━━━━━━━━━━${CSI}39m"
    )

    private fun doPulseTest(pulsePosition: Float, expected: String) {
        checkRender(
            ProgressBar(indeterminate = true, pulsePosition = pulsePosition),
            expected,
            width = 10
        )
    }

    private fun doPercentTest(completed: Long, expected: String, theme: Theme = Theme.PlainAscii) {
        checkRender(
            ProgressBar(completed = completed),
            expected,
            theme = theme,
            width = 5,
            trimIndent = false,
        )
    }
}
