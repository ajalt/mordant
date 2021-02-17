package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.RenderingTest
import com.github.ajalt.mordant.rendering.Theme
import kotlin.test.Test

class ProgressLayoutTest : RenderingTest() {
    @Test
    fun indeterminate() = doTest(
        0,
        expected = "text.txt    0%  ####     0.0/---.-B   ---.-it/s  eta -:--:--"
    )

    @Test
    fun `no progress`() = doTest(
        0, 0,
        expected = "text.txt    0%  ....       0.0/0.0B   ---.-it/s  eta -:--:--"
    )

    @Test
    fun `large values`() = doTest(
        150_000_000, 300_000_000, 1.5, 100_000_000.0,
        expected = "text.txt   50%  ##>.  150.0/300.0MB  100.0Mit/s  eta 0:00:02"
    )

    @Test
    fun `short eta`() = doTest(
        1, 2, 3.0, 4.0,
        expected = "text.txt   50%  ##>.       1.0/2.0B     4.0it/s  eta 0:00:00"
    )

    @Test
    fun `long eta`() = doTest(
        150_000_000, 300_000_000, 1.5, 2.0,
        expected = "text.txt   50%  ##>.  150.0/300.0MB     2.0it/s  eta -:--:--"
    )

    private fun doTest(
        completed: Long,
        total: Long? = null,
        elapsedSeconds: Double = 0.0,
        completedPerSecond: Double? = null,
        expected: String,
    ) = checkRender(
        progressLayout {
            text("text.txt")
            percentage()
            progressBar()
            completed(suffix = "B")
            speed()
            timeRemaining()
        }.build(completed, total, elapsedSeconds, completedPerSecond),
        expected,
        width = 60,
        theme = Theme(Theme.PlainAscii) { strings["progressbar.pending"] = "." },
    )
}
