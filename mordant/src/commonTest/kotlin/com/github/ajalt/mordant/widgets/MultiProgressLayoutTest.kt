package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.test.RenderingTest
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class MultiProgressLayoutTest : RenderingTest() {
    @Test
    fun indeterminate() = doTest(
        completed1 = 0, total1 = 0, elapsed1 = 0.0,
        completed2 = 0, total2 = 0, elapsed2 = 0.0,
        expected = """
        ░Task 1  |  0%|#########################|   0.0/---.-B| ---.-it/s
        ░Task Two|  0%|#########################|   0.0/---.-B| ---.-it/s
        """
    )

    @Test
    @JsName("indeterminate_unaligned")
    fun `indeterminate unaligned`() = doTest(
        completed1 = 0, total1 = 0, elapsed1 = 0.0,
        completed2 = 0, total2 = 0, elapsed2 = 0.0,
        alignColumns = false,
        expected = """
        ░Task 1|  0%|###########################|   0.0/---.-B| ---.-it/s
        ░Task Two|  0%|#########################|   0.0/---.-B| ---.-it/s
        """
    )

    // TODO more tests

    private fun doTest(
        completed1: Long,
        total1: Long,
        elapsed1: Double,
        completed2: Long,
        total2: Long,
        elapsed2: Double,
        expected: String,
        alignColumns: Boolean = true,
    ) {
        val factory: ProgressBarWidgetFactory<String> = progressBarContextLayout(
            spacing = 0,
            alignColumns = alignColumns,
        ) {
            text(TextAlign.LEFT) { context }
            text("|")
            percentage()
            text("|")
            progressBar()
            text("|")
            completed(suffix = "B")
            text("|")
            speed()
        }
        val widget = factory.build(
            ProgressState("Task 1", total1, completed1, elapsed1.seconds),
            ProgressState("Task Two", total2, completed2, elapsed2.seconds),
        )
        checkRender(
            widget,
            expected,
            width = 64,
            theme = Theme(Theme.PlainAscii) { strings["progressbar.pending"] = "." },
        )
    }
}
