package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.rendering.VerticalAlign
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.widgets.progress.*
import com.github.ajalt.mordant.widgets.progress.ProgressState.Status.*
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class ProgressLayoutTest : RenderingTest() {
    private val t = TestTimeSource()
    private val start = t.markNow()
    private val indetermStyle = Theme.Default.style("progressbar.indeterminate")


    @Test
    @JsName("indeterminate_not_started")
    fun `indeterminate not started`() = doTest(
        "text.txt|  0%|#########|     0/---.-B| ---.-/s|eta -:--:--|-:--:--",
        0, started = false
    )

    @Test
    @JsName("indeterminate_started")
    fun `indeterminate started`() = doTest(
        "text.txt|  0%|#########|     0/---.-B| ---.-/s|eta -:--:--|0:00:00",
        0
    )

    @Test
    @JsName("no_progress")
    fun `no progress`() = doTest(
        "text.txt|  0%|.........|         0/0B| ---.-/s|eta -:--:--|0:00:00",
        0, 0
    )

    @Test
    @JsName("large_values")
    fun `large values`() = doTest(
        "text.txt| 50%|####>....|150.0/300.0MB|100.0M/s|eta 0:00:01|4:10:33",
        150_000_000, 300_000_000, 15033.0, 100_000_000.0
    )

    @Test
    @JsName("short_eta")
    fun `short eta`() = doTest(
        "text.txt| 50%|####>....|         1/2B|   4.0/s|eta 0:00:00|0:00:03",
        1, 2, 3.0, 4.0
    )

    @Test
    @JsName("long_eta")
    fun `long eta`() = doTest(
        "text.txt| 50%|####>....|150.0/300.0MB|   2.0/s|eta -:--:--|0:00:01",
        150_000_000, 300_000_000, 1.5, 2.0
    )

    @Test
    @JsName("zero_total")
    fun `zero total`() = doTest(
        "text.txt|  0%|.........|         0/0B| ---.-/s|eta -:--:--|0:00:00",
        0, 0
    )

    @Test
    @JsName("negative_completed_value")
    fun `negative completed value`() = doTest(
        "text.txt|-50%|.........|        -1/2B| ---.-/s|eta -:--:--|0:00:00",
        -1, 2
    )

    @Test
    @JsName("completed_greater_than_total")
    fun `completed value greater than total`() = doTest(
        "text.txt|200%|#########|        10/5B| ---.-/s|eta -:--:--|0:00:00",
        10, 5
    )

    @Test
    @JsName("default_pacing")
    fun `default spacing`() = checkRender(
        progressBarLayout {
            text("|")
            percentage()
            text("|")
            speed()
            text("|")
        }.build(null, 0, start),
        "|    0%  |   ---.-/s  |",
    )

    @Test
    fun pulse() {
        t += 1.seconds
        checkRender(
            progressBarLayout {
                progressBar()
            }.build(null, 0, start, Running(start)),
            indetermStyle("━${TextColors.rgb(1, 1, 1)("━")}━"),
            width = 3,
        )
    }

    @Test
    @JsName("custom_pulse_duration")
    fun `custom pulse duration`() {
        t += 0.5.seconds
        checkRender(
            progressBarLayout {
                progressBar(pulsePeriod = 1.seconds)
            }.build(null, 0, start, Running(start)),
            indetermStyle("━${TextColors.rgb(1, 1, 1)("━")}━"),
            width = 3,
        )
    }

    @Test
    @JsName("no_pulse")
    fun `no pulse`() {
        t += 1.seconds
        checkRender(
            progressBarLayout {
                progressBar(pulsePeriod = ZERO)
            }.build(null, 0, start, Running(start)),
            indetermStyle("━━━"),
            width = 3,
        )
    }

    @Test
    @JsName("timeRemaining_compact")
    fun `timeRemaining compact`() {
        val l = progressBarLayout {
            timeRemaining(compact = true)
        }
        t += 1.minutes
        checkRender(
            l.build(100, 90, start, Running(start), speed = .01),
            "  eta 16:40", // 10remaining/.01hz == 1000s
        )
        checkRender(
            l.build(100, 90, start, Running(start), speed = .001),
            "eta 2:46:40", // 10remaining/.001hz == 10000s
        )
    }

    @Test
    @JsName("layout_no_cells")
    fun `layout with no cells`() {
        val layout = progressBarLayout { }.build(null, 0, start)
        checkRender(layout, "")
    }

    @Test
    @JsName("layout_no_states")
    fun `layout with no states`() {
        val layout = MultiProgressBarWidgetMaker.build(emptyList())
        checkRender(layout, "")
    }

    @Test
    @JsName("eta_and_remaining_compact")
    fun `eta and remaining compact`() = forAll(
        row(null, null, "--:--|  eta --:--"),
        row(0.seconds, 1.seconds, "00:00|  eta 00:01"),
        row(1.seconds, 0.seconds, "00:01|  eta --:--"),
        row(1.hours - 1.seconds, 1.hours - 1.seconds, "59:59|  eta 59:59"),
        row(1.hours, 1.hours, "1:00:00|eta 1:00:00"),
        row(10.hours - 1.seconds, 10.hours - 1.seconds, "9:59:59|eta 9:59:59"),
        row(10.hours, 10.hours, "10:00:00|  eta --:--"),
        row(100.hours, 100.hours, "100:00:00|  eta --:--"),
    ) { elapsed, remaining, expected ->
        val speed = remaining?.let { if (it == ZERO) 0.0 else 100.0 / it.inWholeSeconds }
        val start = setTime(elapsed ?: 0.seconds)
        val status = if (elapsed == null) NotStarted else Running(start)
        val layout = progressBarLayout(spacing = 0) {
            timeElapsed(compact = true)
            text("|")
            timeRemaining(compact = true)
        }.build(100, 0, start, status, speed = speed)
        checkRender(layout, expected)
    }

    @Test
    @JsName("eta_and_remaining_finished")
    fun `eta and remaining finished`() {
        t += 1.hours
        val finishedTime = t.markNow()
        t += 1.hours
        checkRender(
            etaLayout().build(100, 100, start, Finished(start, finishedTime), speed = 10.0),
            "1:00:00|eta -:--:--"
        )
    }

    @Test
    @JsName("eta_and_remaining_paused")
    fun `eta and remaining paused`() {
        t += 1.hours
        val pausedTime = t.markNow()
        t += 1.hours
        val layout = etaLayout().build(100, 50, start, Paused(start, pausedTime), speed = 10.0)
        checkRender(layout, "1:00:00|eta -:--:--")
    }

    @Test
    @JsName("eta_elapsedWhenFinished")
    fun `eta elapsedWhenFinished`() {
        val layout = etaLayout(elapsedWhenFinished = true)
        t += 1.hours

        checkRender(
            layout.build(100, 25, start, Running(start), speed = 1.0),
            "1:00:00|eta 0:01:15"
        )

        val finishedTime = t.markNow()
        t += 1.hours
        checkRender(
            layout.build(100, 100, start, Finished(start, finishedTime), speed = 1.0),
            "1:00:00| in 1:00:00"
        )
    }

    @Test
    fun spinner() = forAll(
        row(0, "1"),
        row(1, "2"),
        row(2, "3"),
        row(3, "1"),
        row(4, "2"),
        row(5, "3"),
    ) { elapsed, expected ->
        val layout = progressBarLayout { spinner(Spinner("123"), fps = 1) }
        val start = setTime(elapsed.seconds)
        checkRender(layout.build(null, 0, start, Running(start)), expected)
    }

    @Test
    fun marquee() = forAll(
        row(0, "   "),
        row(1, "  1"),
        row(2, " 12"),
        row(3, "123"),
        row(4, "234"),
        row(5, "345"),
        row(6, "45 "),
        row(7, "5  "),
        row(8, "   "),
        row(9, "  1"),
    ) { elapsed, expected ->
        val layout = progressBarLayout { marquee("12345", width = 3, fps = 1) }
        val start = setTime(elapsed.seconds)
        checkRender(layout.build(null, 0, start), expected, trimMargin = false)
    }

    @Test
    @JsName("styled_marquee")
    fun `styled marquee`() = forAll(
        row(0, red("   ")),
        row(1, red("  1")),
        row(2, red(" 12")),
        row(3, red("123")),
        row(4, red("234")),
        row(5, red("345")),
        row(6, red("45") + " "),
        row(7, red("5") + "  "),
        row(8, red("   ")),
        row(9, red("  1")),
    ) { elapsed, expected ->
        val layout = progressBarLayout { marquee(red("12345"), width = 3, fps = 1) }
        val start = setTime(elapsed.seconds)
        checkRender(layout.build(null, 0, start), expected, trimMargin = false)
    }

    @Test
    @JsName("completed_decimal_format")
    fun `completed decimal format`() = forAll(
        row(0, 0, null,/*      */" 0/---.-"),
        row(0, 1e1, 1e2,/*     */"  10/100"),
        row(0, 1e2, 1e3,/*     */"    0/1K"),
        row(0, 1e9 - 1, 1e9 - 1, "999/999M"),
        row(1, 0, null,/*      */"     0/---.-"),
        row(1, 1e1, 1e2,/*     */"      10/100"),
        row(1, 1e2, 1e3,/*     */"    0.1/1.0K"),
        row(1, 1e2, 1e6,/*     */"    0.0/1.0M"),
        row(1, 9e5, 1e6,/*     */"    0.9/1.0M"),
        row(1, 9e6, 1e7,/*     */"   9.0/10.0M"),
        row(1, 9e7, 1e8,/*     */" 90.0/100.0M"),
        row(1, 9e8, 1e9,/*     */"    0.9/1.0G"),
        row(1, 1e9 - 1, 1e9 - 1, "999.9/999.9M"),
        row(2, 1e1, 1e2,/*     */"        10/100"),
        row(2, 1e2, 1e3,/*     */"    0.10/1.00K"),
        row(2, 1e2, 1e3,/*     */"    0.10/1.00K"),
        row(2, 1e9 - 1, 1e9 - 1, "999.99/999.99M"),
    ) { precision, completed, total, expected ->
        val layout = progressBarLayout { completed(precision = precision) }
        val widget = layout.build(total?.toLong(), completed.toLong(), start)
        checkRender(widget, expected, trimMargin = false)
    }

    @Test
    @JsName("marquee_scrollWhenContentFits_false")
    fun `marquee scrollWhenContentFits=false`() {
        val layout = progressBarLayout { marquee("123", width = 5) }
        checkRender(layout.build(null, 0, start), "  123", trimMargin = false)
    }

    @Test
    @JsName("marquee_scrollWhenContentFits_true")
    fun `marquee scrollWhenContentFits=true`() {
        val start = setTime(2.seconds)
        val layout = progressBarLayout { marquee("123", width = 5, scrollWhenContentFits = true) }
        checkRender(layout.build(null, 0, start), "23   ", trimMargin = false)
    }


    @Test
    fun verticalAlign() {
        val layout = progressBarLayout {
            text("|\n|\n|")
            text("1")
            text("2", verticalAlign = VerticalAlign.TOP)
            text("3", verticalAlign = VerticalAlign.MIDDLE)
            text("4", verticalAlign = VerticalAlign.BOTTOM)
            text("|\n|\n|")
        }
        checkRender(
            layout.build(null, 0, start), """
            ░|     2        |
            ░|        3     |
            ░|  1        4  |
            """
        )
    }

    @Test
    fun buildCells() {
        val layout = progressBarContextLayout<Int> {
            text { "a$context" }
            text { "b$context" }
        }
        val cells = MultiProgressBarWidgetMaker.buildCells(
            listOf(
                ProgressBarMakerRow(layout, ProgressState(1, 1, 1, start, Running(start))),
                ProgressBarMakerRow(layout, ProgressState(2, 2, 2, start, Running(start))),
            )
        )
        val widget = table {
            body {
                cells.forEach { rowFrom(it) }
            }
        }
        checkRender(
            widget, """
            ░┌────┬────┐
            ░│ a1 │ b1 │
            ░├────┼────┤
            ░│ a2 │ b2 │
            ░└────┴────┘
            """
        )
    }

    @Test
    @JsName("use_as_builder")
    fun `use as builder`() {
        val builder = ProgressLayoutBuilder<Int>()
        builder.text { "a$context" }
        builder.text { "b$context" }
        val layout = builder.build()
        checkRender(
            layout.build(1, 2, 3, start, Running(start)), """
            ░a1  b1
            """
        )
    }

    private fun doTest(
        expected: String,
        completed: Long,
        total: Long? = null,
        elapsedSeconds: Double = 0.0,
        speed: Double? = null,
        started: Boolean = true,
    ) {
        t += elapsedSeconds.seconds
        val status = if (started) Running(start) else NotStarted
        checkRender(
            progressBarLayout(spacing = 0) {
                text("text.txt")
                text("|")
                percentage()
                text("|")
                progressBar()
                text("|")
                completed(suffix = "B")
                text("|")
                speed()
                text("|")
                timeRemaining()
                text("|")
                timeElapsed()
            }.build(total, completed, start, status, speed = speed),
            expected,
            width = 66,
            theme = Theme(Theme.PlainAscii) { strings["progressbar.pending"] = "." },
        )
    }

    private fun etaLayout(elapsedWhenFinished: Boolean = false): ProgressBarDefinition<Unit> {
        return progressBarLayout(spacing = 0) {
            timeElapsed()
            text("|")
            timeRemaining(elapsedWhenFinished = elapsedWhenFinished)
        }
    }

    // this is separate from [t] for use in forAll, which doesn't reset the state between rows
    private fun setTime(elapsed: Duration = 0.seconds): ComparableTimeMark {
        val t = TestTimeSource()
        val start = t.markNow()
        t += elapsed
        return start
    }
}
