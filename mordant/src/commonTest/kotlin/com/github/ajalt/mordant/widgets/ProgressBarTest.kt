package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.test.RenderingTest
import kotlin.js.JsName
import kotlin.test.Test

class ProgressBarTest : RenderingTest() {
    @Test
    @JsName("_0_percent_complete")
    fun `0 percent complete`() = doPercentTest(0, "     ")

    @Test
    @JsName("_10_percent_complete")
    fun `10 percent complete`() = doPercentTest(10, "     ")

    @Test
    @JsName("_20_percent_complete")
    fun `20 percent complete`() = doPercentTest(20, "#>   ")

    @Test
    @JsName("_30_percent_complete")
    fun `30 percent complete`() = doPercentTest(30, "#>   ")

    @Test
    @JsName("_40_percent_complete")
    fun `40 percent complete`() = doPercentTest(40, "##>  ")

    @Test
    @JsName("_60_percent_complete")
    fun `60 percent complete`() = doPercentTest(60, "###> ")

    @Test
    @JsName("_80_percent_complete")
    fun `80 percent complete`() = doPercentTest(80, "####>")

    @Test
    @JsName("_99_percent_complete")
    fun `99 percent complete`() = doPercentTest(99, "####>")

    @Test
    @JsName("_100_percent_complete")
    fun `100 percent complete`() = doPercentTest(100, "#####")

    @Test
    @JsName("default_theme")
    fun `default theme`() = doPercentTest(
        40,
        "${CSI}38;2;97;175;239m━━${CSI}39m ${CSI}38;2;92;99;112m━━${CSI}39m",
        theme = Theme.Default
    )

    @Test
    @JsName("pulse_initial")
    fun `pulse initial`() = doPulseTest(
        pulsePosition = 0f,
        "${CSI}38;2;97;175;239m━━━━━━━━━━${CSI}39m"
    )

    @Test
    @JsName("pulse_25")
    fun `pulse 25`() = doPulseTest(
        pulsePosition = .25f,
        "${CSI}38;2;97;175;239m━${CSI}38;2;251;253;255m━${CSI}38;2;207;230;251m━${CSI}38;2;107;180;240m━${CSI}38;2;97;175;239m━━━━━━${CSI}39m"
    )

    @Test
    @JsName("pulse_50")
    fun `pulse 50`() = doPulseTest(
        pulsePosition = .50f,
        "${CSI}38;2;97;175;239m━━━${CSI}38;2;136;194;244m━${CSI}38;2;239;247;254m━━${CSI}38;2;136;194;244m━${CSI}38;2;97;175;239m━━━${CSI}39m"
    )

    @Test
    @JsName("pulse_75")
    fun `pulse 75`() = doPulseTest(
        pulsePosition = .75f,
        "${CSI}38;2;97;175;239m━━━━━━${CSI}38;2;107;180;240m━${CSI}38;2;207;230;251m━${CSI}38;2;254;255;255m━${CSI}38;2;178;216;249m━${CSI}39m"
    )

    @Test
    @JsName("pulse_100")
    fun `pulse 100`() = doPulseTest(
        pulsePosition = 1f,
        "${CSI}38;2;97;175;239m━━━━━━━━━━${CSI}39m"
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
