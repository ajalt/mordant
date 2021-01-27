package com.github.ajalt.mordant.widgets

import com.github.ajalt.colormath.HSL
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.math.absoluteValue
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt

class ProgressBar(
    private var total: Long = 100,
    private var completed: Long = 0,
    private var indeterminate: Boolean = false,
    private val width: Int? = null,
    private val pulsePosition: Float? = null,
    private val pendingChar: String? = null,
    private val separatorChar: String? = null,
    private val completeChar: String? = null,
    private val pendingStyle: TextStyle? = null,
    private val separatorStyle: TextStyle? = null,
    private val completeStyle: TextStyle? = null,
    private val finishedStyle: TextStyle? = null,
    private val indeterminateStyle: TextStyle? = null,
) : Widget {
    init {
        require(width == null || width > 0) { "width must be greater than 0" }
    }

    val percentComplete: Double get() = if (total == 0L) 0.0 else (completed.toDouble() / total).coerceIn(0.0, 1.0)

    private fun width(renderWidth: Int) = width ?: renderWidth

    override fun measure(t: Terminal, width: Int): WidthRange {
        val w = width(width)
        return WidthRange(w.coerceAtMost(4), w)
    }

    override fun render(t: Terminal, width: Int): Lines {
        val w = width(width)
        val completedLength = if (indeterminate) w else (percentComplete * w).toInt()

        val pc = pendingChar ?: t.theme.string("progressbar.pending")
        val sc = separatorChar ?: t.theme.string("progressbar.separator")
        val cc = completeChar ?: t.theme.string("progressbar.complete")

        val ps = pendingStyle ?: t.theme.style("progressbar.pending")
        val ss = separatorStyle ?: t.theme.style("progressbar.separator")
        val fs = finishedStyle ?: t.theme.style("progressbar.finished")
        val cs = when {
            indeterminate -> indeterminateStyle ?: t.theme.style("progressbar.indeterminate")
            else -> completeStyle ?: t.theme.style("progressbar.complete")
        }

        if (completedLength == w && !indeterminate) {
            return makeLine(listOfNotNull(segmentText(cc, w, fs)))
        }

        val sep = if (completedLength in 1 until w) segmentText(sc, 1, ss) else null
        val sepLength = sep?.cellWidth ?: 0
        val complete = makeComplete(w, completedLength, cc, cs)
        val pending = segmentText(pc, w - completedLength - sepLength, ps)

        return makeLine(complete + listOfNotNull(sep, pending))
    }

    private fun makeComplete(width: Int, barLength: Int, char: String, style: TextStyle): Line {
        if (barLength == 0) return emptyList()

        val color = style.color?.toHSL()

        if (color == null || pulsePosition == null) {
            return listOfNotNull(segmentText(char, barLength, style))
        }

        // x is offset left by half a period so that the pulse starts offscreen
        val offset = 2 * (pulsePosition % 1.0).absoluteValue * width - width / 2f
        return List(barLength) {
            // gaussian with σ²=0.1 and x scaled to ~50% of width
            val x = 2 * (it - offset) / width
            val gauss = exp(-x.pow(2.0) * 5)
            // linear interpolate the luminosity between the original and white
            val l = color.l + ((100 - color.l) * gauss).roundToInt()
            Span.word(char, TextStyle(HSL(color.h, color.s, l)))
        }
    }

    private fun makeLine(line: Line): Lines {
        return Lines(listOf(line))
    }

    private fun segmentText(char: String, count: Int, style: TextStyle): Span? {
        val text = char.repeat(count)
        return if (text.isEmpty()) null else Span.word(text, style)
    }
}
