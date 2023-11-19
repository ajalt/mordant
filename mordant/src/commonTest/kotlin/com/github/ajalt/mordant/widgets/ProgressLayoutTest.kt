package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.test.RenderingTest
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class ProgressLayoutTest : RenderingTest() {
    private val t = TestTimeSource()
    private val now = t.markNow()
    private val indetermStyle = Theme.Default.style("progressbar.indeterminate")

    @Test
    fun indeterminate() = doTest(
        "text.txt|  0%|#########|   0.0/---.-B| ---.-it/s|eta -:--:--|0:00:00",
        0
    )

    @Test
    @JsName("no_progress")
    fun `no progress`() = doTest(
        "text.txt|  0%|.........|     0.0/0.0B| ---.-it/s|eta -:--:--|0:00:00",
        0, 0
    )

    @Test
    @JsName("large_values")
    fun `large values`() = doTest(
        "text.txt| 50%|####>....|150.0/300.0MB|100.0Mit/s|eta 0:00:01|4:10:33",
        150_000_000, 300_000_000, 15033.0, 100_000_000.0
    )

    @Test
    @JsName("short_eta")
    fun `short eta`() = doTest(
        "text.txt| 50%|####>....|     1.0/2.0B|   4.0it/s|eta 0:00:00|0:00:03",
        1, 2, 3.0, 4.0
    )

    @Test
    @JsName("long_eta")
    fun `long eta`() = doTest(
        "text.txt| 50%|####>....|150.0/300.0MB|   2.0it/s|eta -:--:--|0:00:01",
        150_000_000, 300_000_000, 1.5, 2.0
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
        }.build(null, 0, now),
        "|    0%  |   ---.-it/s  |",
    )

    @Test
    fun pulse() {
        t += 1.seconds
        checkRender(
            progressBarLayout {
                progressBar()
            }.build(null, 0, now, now),
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
                progressBar(showPulse = false)
            }.build(null, 0, now, now),
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
            l.build(100, 90, now, speed = .01),
            "  eta 16:40", // 10remaining/.01hz == 1000s
        )
        checkRender(
            l.build(100, 90, now, speed = .001),
            "eta 2:46:40", // 10remaining/.001hz == 10000s
        )
    }

    private fun doTest(
        expected: String,
        completed: Long,
        total: Long? = null,
        elapsedSeconds: Double = 0.0,
        speed: Double? = null,
    ) {
        t += elapsedSeconds.seconds
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
            }.build(total, completed, now, now, speed=speed),
            expected,
            width = 68,
            theme = Theme(Theme.PlainAscii) { strings["progressbar.pending"] = "." },
        )
    }
}
