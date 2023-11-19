package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.test.RenderingTest
import kotlin.js.JsName
import kotlin.math.max
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class MultiProgressLayoutTest : RenderingTest() {
    @Test
    fun indeterminate() = doTest(
        completed1 = 0, total1 = null, elapsed1 = null,
        completed2 = 0, total2 = null, elapsed2 = null,
        expected = """
        ░Task 1  |  0%|##############|   0.0/---.-| ---.-it/s|eta -:--:--
        ░Task Two|  0%|##############|   0.0/---.-| ---.-it/s|eta -:--:--
        """
    )

    @Test
    @JsName("indeterminate_unaligned")
    fun `indeterminate unaligned`() = doTest(
        completed1 = 0, total1 = null, elapsed1 = null,
        completed2 = 0, total2 = null, elapsed2 = null,
        alignColumns = false,
        expected = """
        ░Task 1|  0%|################|   0.0/---.-| ---.-it/s|eta -:--:--
        ░Task Two|  0%|##############|   0.0/---.-| ---.-it/s|eta -:--:--
        """
    )

    @Test
    @JsName("one_in_progress")
    fun `one in progress`() = doTest(
        completed1 = 5, total1 = 10, elapsed1 = 5.0,
        completed2 = 0, total2 = null, elapsed2 = null,
        expected = """
        ░Task 1  | 50%|#######>......|    5.0/10.0|   1.0it/s|eta 0:00:05
        ░Task Two|  0%|##############|   0.0/---.-| ---.-it/s|eta -:--:--
        """
    )

    @Test
    @JsName("two_finished")
    fun `two finished`() = doTest(
        completed1 = 5, total1 = 10, elapsed1 = 5.0,
        completed2 = 20, total2 = 20, elapsed2 = 10.0,
        expected = """
        ░Task 1  | 50%|#######>......|    5.0/10.0|   1.0it/s|eta 0:00:05
        ░Task Two|100%|##############|   20.0/20.0|   2.0it/s|eta 0:00:00
        """
    )

    private fun doTest(
        completed1: Long,
        total1: Long?,
        elapsed1: Double?,
        completed2: Long,
        total2: Long?,
        elapsed2: Double?,
        expected: String,
        alignColumns: Boolean = true,
    ) {
        val t = TestTimeSource()
        t += max(elapsed1 ?: 0.0, elapsed2 ?: 0.0).seconds
        val now = t.markNow()
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
            completed()
            text("|")
            speed()
            text("|")
            timeRemaining()
        }
        val widget = factory.build(
            ProgressState(
                "Task 1",
                total1,
                completed1,
                now,
                now.takeIf { elapsed1 != null },
                speed = elapsed1?.let { calcHz(completed1, it.seconds) }
            ),
            ProgressState(
                "Task Two",
                total2,
                completed2,
                now,
                now.takeIf { elapsed2 != null },
                speed = elapsed2?.let { calcHz(completed2, it.seconds) }
            ),
        )
        checkRender(
            widget,
            expected,
            width = 64,
            theme = Theme(Theme.PlainAscii) { strings["progressbar.pending"] = "." },
        )
    }
}
