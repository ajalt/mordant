package com.github.ajalt.mordant.widgets

import com.github.ajalt.colormath.model.Oklab
import com.github.ajalt.colormath.model.RGB
import com.github.ajalt.colormath.transform.EasingFunctions
import com.github.ajalt.colormath.transform.interpolator
import com.github.ajalt.colormath.transform.sequence
import com.github.ajalt.mordant.internal.EMPTY_LINE
import com.github.ajalt.mordant.internal.ThemeFlag
import com.github.ajalt.mordant.internal.ThemeString
import com.github.ajalt.mordant.internal.ThemeStyle
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal

/**
 * A horizontal bar that tracks progress.
 *
 * The bar has three states:
 *
 * - indeterminate: the bar will draw its full width and show a pulsing animation based on `pulsePosition`
 * - in progress: the bar will draw a width proportional to `fractionComplete`, or `completed / total`
 * - finished: the bar will draw its full width with no pulse.
 */
class ProgressBar private constructor(
    private val fractionComplete: Float,
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
    /**
     * @param total The bar draws the fraction complete as [completed] `/` [total]
     * @param completed The bar draws the fraction complete as [completed] `/` [total]
     * @param indeterminate If true, [total] and [completed] are ignored and the bar draws in an indeterminate state
     * @param width The width in characters for this widget
     * @param pulsePosition When [indeterminate] is true, the pulse animation is drawn at a fraction along the bar calculated by `pulsePosition % 1.0`
     * @param showPulse (theme flag: "progressbar.pulse") If false, never draw the pulse animation in the indeterminate state.
     * @param pendingChar (theme string: "progressbar.pending") The character to use to draw the pending portion of the bar in the active state.
     * @param separatorChar (theme string: "progressbar.separator") The character to draw in between the competed and pending bar in the active state.
     * @param completeChar (theme string: "progressbar.complete") The character to use to draw the completed portion of the bar in the active state.
     * @param pendingStyle(theme style: "progressbar.pending") The style to use for the [pendingChar]s
     * @param separatorStyle (theme style: "progressbar.separator") The style to use for the [separatorChar]
     * @param completeStyle (theme style: "progressbar.complete") The style to use for the [completeChar] when [completed] < [total]
     * @param finishedStyle (theme style: "progressbar.complete") The style to use for the [completeChar] when [total] <= [completed]
     * @param indeterminateStyle e (theme style: "progressbar.separator") The style to use when [indeterminate] is true
     */
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
        if (total <= 0) 0f else completed.toFloat() / total,
        indeterminate,
        width,
        pulsePosition,
        showPulse,
        pendingChar,
        separatorChar,
        completeChar,
        pendingStyle,
        separatorStyle,
        completeStyle,
        finishedStyle,
        indeterminateStyle
    )

    /**
     * @param fractionComplete A number between 0 and 1, with 0 drawing all [pendingChar]s, and 1 drawing all [completeChar]s
     * @param indeterminate If true, [fractionComplete] is ignored and the bar draws in an indeterminate state
     * @param width The width in characters for this widget
     * @param pulsePosition When [indeterminate] is true, the pulse animation is drawn at a fraction along the bar calculated by `pulsePosition % 1.0`
     * @param showPulse (theme flag: "progressbar.pulse") If false, never draw the pulse animation in the indeterminate state.
     * @param pendingChar (theme string: "progressbar.pending") The character to use to draw the pending portion of the bar in the active state.
     * @param separatorChar (theme string: "progressbar.separator") The character to draw in between the competed and pending bar in the active state.
     * @param completeChar (theme string: "progressbar.complete") The character to use to draw the completed portion of the bar in the active state.
     * @param pendingStyle(theme style: "progressbar.pending") The style to use for the [pendingChar]s
     * @param separatorStyle (theme style: "progressbar.separator") The style to use for the [separatorChar]
     * @param completeStyle (theme style: "progressbar.complete") The style to use for the [completeChar] when [fractionComplete] < 1
     * @param finishedStyle (theme style: "progressbar.complete") The style to use for the [completeChar] when [fractionComplete] == 1
     * @param indeterminateStyle e (theme style: "progressbar.separator") The style to use when [indeterminate] is true
     */
    constructor(
        fractionComplete: Float = 0f,
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
        fractionComplete,
        indeterminate,
        width,
        pulsePosition,
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

    private fun width(renderWidth: Int) = width ?: renderWidth

    override fun measure(t: Terminal, width: Int): WidthRange {
        val w = width(width)
        return WidthRange(w.coerceAtMost(3), w)
    }

    override fun render(t: Terminal, width: Int): Lines {
        val w = width(width)
        val completedLength = when {
            indeterminate -> w
            else -> (fractionComplete.coerceIn(0f..1f) * w).toInt()
        }
        val completeStyle = when {
            indeterminate -> indeterminateStyle[t]
            else -> completeStyle[t]
        }

        if (completedLength == w && !indeterminate) {
            return makeLine(listOfNotNull(segmentText(completeChar[t], w, finishedStyle[t])))
        }

        val sep = if (completedLength in 1 until w) segmentText(
            separatorChar[t], 1, separatorStyle[t]
        ) else null
        val sepLength = sep?.cellWidth ?: 0
        val complete = makeComplete(t, w, completedLength, completeChar[t], completeStyle)
        val pending = segmentText(pendingChar[t], w - completedLength - sepLength, pendingStyle[t])

        return makeLine(complete + listOfNotNull(sep, pending))
    }

    private fun makeComplete(
        t: Terminal,
        width: Int,
        barLength: Int,
        char: String,
        style: TextStyle,
    ): Line {
        if (barLength == 0) return EMPTY_LINE

        val color = style.color

        if (color == null || !showPulse[t] || width < 2) {
            return Line(listOfNotNull(segmentText(char, barLength, style)))
        }

        val p = (pulsePosition % 1.0) * 1.5
        val lerp = Oklab.interpolator {
            easing = EasingFunctions.easeInOut()
            stop(color, p - .5)
            stop(RGB(1, 1, 1), p - .25)
            stop(color, p)
        }.sequence(width)
            .take(barLength)
            // as an optimization, convert to RGBInt so that adjacent identical styles can be joined
            // even if the float values are slightly different
            .map { Span.word(char, TextStyle(it.toSRGB().toRGBInt())) }
        return Line(lerp.toList())
    }

    private fun makeLine(line: List<Span>): Lines {
        return Lines(listOf(Line(line)))
    }

    private fun segmentText(char: String, count: Int, style: TextStyle): Span? {
        val text = char.repeat(count)
        return if (text.isEmpty()) null else Span.word(text, style)
    }
}
