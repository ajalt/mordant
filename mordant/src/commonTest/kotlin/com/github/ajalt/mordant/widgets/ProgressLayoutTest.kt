package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.test.RenderingTest
import kotlin.js.JsName
import kotlin.test.Test

class ProgressLayoutTest : RenderingTest() {
    private val indetermStyle = Theme.Default.style("progressbar.indeterminate")

    @Test
    fun indeterminate() = doTest(
        0,
        expected = "text.txt|  0%|#########################|   0.0/---.-B| ---.-it/s"
    )

    @Test
    @JsName("no_progress")
    fun `no progress`() = doTest(
        0, 0,
        expected = "text.txt|  0%|.........................|     0.0/0.0B| ---.-it/s"
    )

    @Test
    @JsName("large_values")
    fun `large values`() = doTest(
        150_000_000, 300_000_000, 1.5, 100_000_000.0,
        expected = "text.txt| 50%|############>............|150.0/300.0MB|100.0Mit/s"
    )

    @Test
    @JsName("short_eta")
    fun `short eta`() = doTest(
        1, 2, 3.0, 4.0,
        expected = "text.txt| 50%|############>............|     1.0/2.0B|   4.0it/s"
    )

    @Test
    @JsName("automatic_eta")
    fun `automatic eta`() = doTest(
        1, 2, 3.0,
        expected = "text.txt| 50%|############>............|     1.0/2.0B|   0.3it/s"
    )

    @Test
    @JsName("long_eta")
    fun `long eta`() = doTest(
        150_000_000, 300_000_000, 1.5, 2.0,
        expected = "text.txt| 50%|############>............|150.0/300.0MB|   2.0it/s"
    )

    @Test
    fun defaultPadding() = checkRender(
        progressLayout {
            text("1")
            percentage()
            text("2")
            speed()
            text("3")
        }.build(0, 0, 0.0, 0.0),
        "1    0%  2   ---.-it/s  3",
    )

    @Test
    fun pulse() = checkRender(
        progressLayout {
            progressBar()
        }.build(0, null, 1.0, 0.0),
        indetermStyle("━${TextColors.rgb(1, 1, 1)("━")}━"),
        width = 3,
    )

    @Test
    @JsName("no_pulse")
    fun `no pulse`() {
        checkRender(
            progressLayout {
                progressBar(showPulse = false)
            }.build(0, null, 1.0, 0.0),
            indetermStyle("━━━"),
            width = 3,
        )
    }

    private fun doTest(
        completed: Long,
        total: Long? = null,
        elapsedSeconds: Double = 0.0,
        completedPerSecond: Double? = null,
        expected: String,
    ) = checkRender(
        progressLayout {
            padding = 0
            text("text.txt")
            text("|")
            percentage()
            text("|")
            progressBar()
            text("|")
            completed(suffix = "B")
            text("|")
            speed()
        }.build(completed, total, elapsedSeconds, completedPerSecond),
        expected,
        width = 64,
        theme = Theme(Theme.PlainAscii) { strings["progressbar.pending"] = "." },
    )
}
