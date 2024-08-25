package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.widgets.progress.*
import com.github.ajalt.mordant.widgets.progress.ProgressState.Status.*
import kotlin.js.JsName
import kotlin.math.max
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class MultiProgressLayoutTest : RenderingTest() {
    @Test
    fun indeterminate() = doTest(
        completed1 = 0, total1 = null, elapsed1 = null, speed1 = null,
        completed2 = 0, total2 = null, elapsed2 = null, speed2 = null,
        expected = """
        ░Task 1  |  0%|##############|     0/---.-| ---.-/s|eta -:--:--
        ░Task Two|  0%|##############|     0/---.-| ---.-/s|eta -:--:--
        """
    )

    @[Test JsName("indeterminate_unaligned")]
    fun `indeterminate unaligned`() = doTest(
        completed1 = 0, total1 = null, elapsed1 = null, speed1 = null,
        completed2 = 0, total2 = null, elapsed2 = null, speed2 = null,
        alignColumns = false,
        expected = """
        ░Task 1|  0%|################|     0/---.-| ---.-/s|eta -:--:--
        ░Task Two|  0%|##############|     0/---.-| ---.-/s|eta -:--:--
        """
    )

    @[Test JsName("one_in_progress")]
    fun `one in progress`() = doTest(
        completed1 = 5, total1 = 10, elapsed1 = 5.0, speed1 = 1.0,
        completed2 = 0, total2 = null, elapsed2 = null, speed2 = null,
        expected = """
        ░Task 1  | 50%|#######>......|        5/10|   1.0/s|eta 0:00:05
        ░Task Two|  0%|##############|     0/---.-| ---.-/s|eta -:--:--
        """
    )

    @[Test JsName("two_finished")]
    fun `two finished`() = doTest(
        completed1 = 5, total1 = 10, elapsed1 = 5.0, speed1 = 1.0,
        completed2 = 20, total2 = 20, elapsed2 = 10.0, speed2 = 2.0,
        expected = """
        ░Task 1  | 50%|#######>......|        5/10|   1.0/s|eta 0:00:05
        ░Task Two|100%|##############|       20/20|   2.0/s|eta 0:00:00
        """
    )

    @[Test JsName("different_layouts")]
    fun `different layouts`() {
        val t = TestTimeSource()
        val animTime = t.markNow()
        t += 5.seconds
        val now = t.markNow()
        val definition1 = progressBarContextLayout(spacing = 0) {
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
        val definition2 = progressBarContextLayout(spacing = 0) {
            text("b")
            text("|")
            text { context }
            text("|")
            progressBar()
        }
        val definition3 = progressBarContextLayout(spacing = 0, alignColumns = false) {
            text("cc")
            text("|")
            text { context }
            text("|")
            progressBar()
        }
        val widget = MultiProgressBarWidgetMaker.build(
            definition1 to ProgressState(
                context = "Task 1",
                total = 10,
                completed = 5,
                animationTime = animTime,
                status = Running(now - 5.seconds),
                speed = 1.0,
            ),
            definition1 to ProgressState(
                context = "Task 2",
                total = 10,
                completed = 5,
                animationTime = animTime,
                status = NotStarted,
                speed = 1.0,
            ),
            definition2 to ProgressState(
                context = "Task 3",
                total = 2,
                completed = 1,
                animationTime = animTime,
                status = Running(now - 10.seconds),
            ),
            definition3 to ProgressState(
                context = "Task 4",
                total = 2,
                completed = 1,
                animationTime = animTime,
                status = Running(now - 10.seconds),
            ),
        )
        checkRender(
            widget,
            """
            ░Task 1|   50%|#######>......|        5/10|   1.0/s|eta 0:00:05░
            ░Task 2|   50%|#######>......|        5/10|   1.0/s|eta -:--:--░
            ░     b|Task 3|#######>......                                  ░
            ░cc|Task 4|##########################>.........................░
            """,
            width = 62,
            theme = Theme(Theme.PlainAscii) { strings["progressbar.pending"] = "." },
        )
    }

    private fun doTest(
        completed1: Long,
        total1: Long?,
        elapsed1: Double?,
        completed2: Long,
        speed1: Double? = null,
        total2: Long?,
        elapsed2: Double?,
        speed2: Double? = null,
        expected: String,
        alignColumns: Boolean = true,
    ) {
        val t = TestTimeSource()
        val animTime = t.markNow()
        t += max(elapsed1 ?: 0.0, elapsed2 ?: 0.0).seconds
        val now = t.markNow()
        val definition = progressBarContextLayout(spacing = 0, alignColumns = alignColumns) {
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
        val widget = MultiProgressBarWidgetMaker.build(
            definition to ProgressState(
                context = "Task 1",
                total = total1,
                completed = completed1,
                animationTime = animTime,
                status = if (elapsed1 == null) NotStarted else Running(now - elapsed1.seconds),
                speed = speed1,
            ),
            definition to ProgressState(
                context = "Task Two",
                total = total2,
                completed = completed2,
                animationTime = animTime,
                status = if (elapsed2 == null) NotStarted else Running(now - elapsed2.seconds),
                speed = speed2,
            ),
        )
        checkRender(
            widget,
            expected,
            width = 62,
            theme = Theme(Theme.PlainAscii) { strings["progressbar.pending"] = "." },
        )
    }
}
