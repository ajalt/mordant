package com.github.ajalt.mordant.widgets

import com.github.ajalt.colormath.HSL
import com.github.ajalt.mordant.internal.ThemeFlag
import com.github.ajalt.mordant.internal.ThemeString
import com.github.ajalt.mordant.internal.ThemeStyle
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.math.absoluteValue
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt

class ProgressBar private constructor(
    private var total: Long,
    private var completed: Long,
    private var indeterminate: Boolean,
    private val width: Int?,
    private val pulsePosition: Float,
    private val showPulse: ThemeFlag,
    private val pendingChar: ThemeString,
    private val separatorChar: ThemeString,
    private val completeChar: ThemeString,
    private val pendingStyle: ThemeStyle,
    private val separatorStyle: ThemeStyle,
    private val completeStyle: ThemeStyle,
    private val finishedStyle: ThemeStyle,
    private val indeterminateStyle: ThemeStyle,
) : Widget {
    constructor(
        total: Long = 100,
        completed: Long = 0,
        indeterminate: Boolean = false,
        width: Int? = null,
        pulsePosition: Float = 0f,
        showPulse: Boolean? = null,
        pendingChar: String? = null,
        separatorChar: String? = null,
        completeChar: String? = null,
        pendingStyle: TextStyle? = null,
        separatorStyle: TextStyle? = null,
        completeStyle: TextStyle? = null,
        finishedStyle: TextStyle? = null,
        indeterminateStyle: TextStyle? = null,
    ) : this(
        total, completed, indeterminate, width, pulsePosition,
        ThemeFlag.of("progressbar.pulse", showPulse),
        ThemeString.of("progressbar.pending", pendingChar),
        ThemeString.of("progressbar.separator", separatorChar),
        ThemeString.of("progressbar.complete", completeChar),
        ThemeStyle.of("progressbar.pending", pendingStyle),
        ThemeStyle.of("progressbar.separator", separatorStyle),
        ThemeStyle.of("progressbar.complete", completeStyle),
        ThemeStyle.of("progressbar.finished", finishedStyle),
        ThemeStyle.of("progressbar.indeterminate", indeterminateStyle),
    )

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
        val completeStyle = when {
            indeterminate -> indeterminateStyle[t]
            else -> completeStyle[t]
        }

        if (completedLength == w && !indeterminate) {
            return makeLine(listOfNotNull(segmentText(completeChar[t], w, finishedStyle[t])))
        }

        val sep = if (completedLength in 1 until w) segmentText(separatorChar[t], 1, separatorStyle[t]) else null
        val sepLength = sep?.cellWidth ?: 0
        val complete = makeComplete(t, w, completedLength, completeChar[t], completeStyle)
        val pending = segmentText(pendingChar[t], w - completedLength - sepLength, pendingStyle[t])

        return makeLine(complete + listOfNotNull(sep, pending))
    }

    private fun makeComplete(t: Terminal, width: Int, barLength: Int, char: String, style: TextStyle): Line {
        if (barLength == 0) return emptyList()

        val color = style.color?.toHSL()

        if (color == null || !showPulse[t]) {
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
