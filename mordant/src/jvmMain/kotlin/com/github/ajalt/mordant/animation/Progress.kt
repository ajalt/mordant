package com.github.ajalt.mordant.animation

import com.github.ajalt.colormath.HSL
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt

class ProgressBar(
    private var total: Int = 100,
    private var completed: Int = 0,
    private val width: Int? = null,
    private val pulse: Boolean? = null,
    private val pendingChar: String? = null,
    private val separatorChar: String? = null,
    private val completeChar: String? = null,
    private val pendingStyle: TextStyle? = null,
    private val separatorStyle: TextStyle? = null,
    private val completeStyle: TextStyle? = null,
    private val finishedStyle: TextStyle? = null,
    private val pulseDuration: Int = 10
) : Renderable {
    init {
        require(pulseDuration > 1) { "pulse duration must be greater than 1" }
    }

    private class Cache(val width: Int, val completedLength: Int, val pulseFrame: Int, val lines: Lines)

    fun update(completed: Int) {
        this.completed = completed
    }

    fun update(completed: Int, total: Int) {
        this.completed = completed
        this.total = total
    }

    fun advancePulse() {
        pulseFrame = (pulseFrame + 1) % (pulseDuration * 2)
    }

    val percentComplete: Float get() = (completed.toFloat() / total).coerceIn(0f, 1f)

    // Cache output for performance since these will usually be rendered many times
    private var cache = Cache(-1, -1, -1, EMPTY_LINES)
    private var pulseFrame = 0

    private fun width(renderWidth: Int) = width ?: renderWidth

    override fun measure(t: Terminal, width: Int): WidthRange {
        return WidthRange(width(width), width(width))
    }

    override fun render(t: Terminal, width: Int): Lines {
        val w = width(width)
        val completedLength = (percentComplete * w).toInt()

        if (w == cache.width &&
            completedLength == cache.completedLength &&
            pulseFrame == cache.pulseFrame
        ) {
            return cache.lines
        }


        val cc = completeChar ?: t.theme.string("progressbar.complete")
        val pc = pendingChar ?: t.theme.string("progressbar.pending")
        val sc = separatorChar ?: t.theme.string("progressbar.separator")

        val cs = completeStyle ?: t.theme.style("progressbar.complete")
        val ps = pendingStyle ?: t.theme.style("progressbar.pending")
        val ss = separatorStyle ?: t.theme.style("progressbar.separator")
        val fs = finishedStyle ?: t.theme.style("progressbar.finished")

        if (completedLength == w) {
            return makeLines(w, completedLength, listOfNotNull(segmentText(cc, w, fs)))
        }

        val sep = when {
            completedLength > 0 -> segmentText(sc, 1, ss)
            else -> null
        }
        val sepLength = sep?.cellWidth ?: 0
        val complete = makeComplete(t, w, completedLength, cc, cs)
        val pending = segmentText(pc, w - completedLength - sepLength, ps)


        return makeLines(w, completedLength, (complete + listOfNotNull(sep, pending)))
    }

    private fun makeComplete(t: Terminal, width: Int, barLength: Int, char: String, style: TextStyle): Line {
        if (barLength == 0) return emptyList()

        val pulse = this.pulse ?: t.theme.flag("progressbar.pulse")
        val color = style.color?.toHSL()

        if (color == null || !pulse) {
            return listOfNotNull(segmentText(char, barLength, style))
        }

        fun l(lerp: Double): Int {
            return color.l + ((100 - color.l) * lerp).roundToInt()
        }

        // gaussian with σ²=0.1 and x scaled to ~20% of width
        fun gauss(x: Double): Double {
            return exp(-(x / (width * .2)).pow(2.0) * 5)
        }

        val line = mutableListOf<Span>()
        for (it in 0 until barLength) {
            // x is offset left by half a period so that the pulse starts offscreen
            val x = it - pulseFrame.toDouble() * width / pulseDuration + pulseDuration / 2
            line += Span.word(char, TextStyle(HSL(color.h, color.s, l(gauss(x.toDouble())))))
        }

        return line
    }

    private fun makeLines(width: Int, completedLength: Int, line: Line): Lines {
        return Lines(listOf(line)).also {
            cache = Cache(width, completedLength, pulseFrame, it)
        }
    }

    private fun segmentText(char: String, count: Int, style: TextStyle): Span? {
        val text = char.repeat(count)
        return if (text.isEmpty()) null else Span.word(text, style)
    }
}
