package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.BlankWidgetWrapper
import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.formatMultipleWithSiSuffixes
import com.github.ajalt.mordant.internal.formatWithSiSuffix
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.Whitespace
import com.github.ajalt.mordant.table.ColumnWidth
import kotlin.math.roundToInt
import kotlin.time.DurationUnit

/**
 * Add a fixed text cell to this layout.
 *
 * @param content The text to display in this cell.
 * @param align The text alignment for this cell. Cells are right-aligned by default.
 */
fun ProgressBarBuilder<*>.text(content: String, align: TextAlign? = null) {
    cell(align = align) { Text(content) }
}

/**
 * Add a dynamic text cell to this layout.
 *
 * The [content] lambda will be called with the current progress state as its receiver.
 *
 * ### Example
 * ```
 * text { context.toString() }
 * ```
 *
 * @param align The text alignment for this cell. Cells are right-aligned by default.
 * @param content A lambda returning the text to display in this cell.
 *
 */
fun <T> ProgressBarBuilder<T>.text(
    align: TextAlign? = null,
    content: ProgressState<T>.() -> String,
) {
    cell(align = align) { Text(content()) }
}

/**
 * Add a cell that displays the current completed count to this layout.
 *
 * @param suffix A string to append to the end of the displayed count, such as "B" if you are tracking bytes. Empty by default.
 * @param includeTotal If true, the total count will be displayed after the completed count, separated by a slash. True by default.
 * @param style The style to use for the displayed count.
 * @param fps The number of times per second to update the displayed count. 5 by default.
 */
fun ProgressBarBuilder<*>.completed(
    suffix: String = "",
    includeTotal: Boolean = true,
    style: TextStyle = DEFAULT_STYLE,
    fps: Int = 5,
) = cell(
    // " 100.0M"
    // " 100.0/200.0M"
    ColumnWidth.Fixed((if (includeTotal) 12 else 6) + suffix.length),
    fps = fps,
) {
    val complete = completed.toDouble()
    val total = total
    val (nums, unit) = formatMultipleWithSiSuffixes(1, complete, total.toDouble())

    val t = nums[0] + when {
        includeTotal && total > 0 -> "/${nums[1]}$unit"
        includeTotal && total <= 0 -> "/---.-"
        else -> ""
    } + suffix
    Text(style(t), whitespace = Whitespace.PRE)
}

/**
 * Add a cell that displays the current speed to this layout.
 *
 * @param suffix A string to append to the end of the displayed speed, such as "B/s" if you are tracking bytes. "it/s" by default.
 * @param style The style to use for the displayed speed.
 * @param fps The number of times per second to update the displayed speed. 5 by default.
 */
fun ProgressBarBuilder<*>.speed(
    suffix: String = "it/s",
    style: TextStyle = DEFAULT_STYLE,
    fps: Int = 5,
) = cell(
    ColumnWidth.Fixed(6 + suffix.length), // " 100.0M"
    fps = fps
) {
    val t = when {
        isIndeterminate || completedPerSecond <= 0 -> "---.-"
        else -> completedPerSecond.formatWithSiSuffix(1)
    }
    Text(style(t + suffix), whitespace = Whitespace.PRE)
}

/**
 * Add a cell that displays the current percentage to this layout.
 *
 * @param fps The number of times per second to update the displayed percentage. 5 by default.
 */
fun ProgressBarBuilder<*>.percentage(fps: Int = 5) = cell(
    ColumnWidth.Fixed(4),  // " 100%"
    fps = fps
) {
    val total = total
    val percent = when {
        total <= 0 -> 0
        else -> (100.0 * completed / total).toInt()
    }
    Text("$percent%")
}

/**
 * Add a cell that displays the time remaining to this layout.
 *
 * @param prefix A string to prepend to the displayed time, such as "eta " or "time left: ". "eta " by default.
 * @param style The style to use for the displayed time.
 * @param fps The number of times per second to update the displayed time. 5 by default.
 */
fun ProgressBarBuilder<*>.timeRemaining(
    prefix: String = "eta ",
    style: TextStyle = DEFAULT_STYLE,
    fps: Int = 5,
) = cell(
    ColumnWidth.Fixed(7 + prefix.length), // " 0:00:02"
    fps = fps
) {
    fun widget(s: String) = Text(style(s), whitespace = Whitespace.PRE)

    val total = total
    val eta = if (total <= 0) 0.0 else (total - completed) / completedPerSecond
    val maxEta = 35_999 // 9:59:59
    if (isIndeterminate || eta < 0 || completedPerSecond == 0.0 || eta > maxEta) {
        return@cell widget("$prefix-:--:--")
    }

    val h = (eta / (60 * 60)).toInt()
    val m = (eta / 60 % 60).toInt()
    val s = (eta % 60).roundToInt()

    widget("$prefix$h:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}")
}

/**
 * Add a [Spinner] to this layout.
 *
 * ### Example
 * ```
 * spinner(Spinner.Dots())
 * ```
 *
 * @param spinner The spinner to display
 * @param fps The number of times per second to advance the spinner's displayed frame. 8 by default.
 */
fun ProgressBarBuilder<*>.spinner(spinner: Spinner, fps: Int = 8) = cell(fps = fps) {
    spinner.tick = (elapsed.toDouble(DurationUnit.SECONDS) / fps).toInt()
    if (isFinished) BlankWidgetWrapper(spinner)
    else spinner
}

/**
 * Add a progress bar cell to this layout.
 *
 * @param width The width in characters for this widget
 * @param pendingChar (theme string: "progressbar.pending") The character to use to draw the pending portion of the bar in the active state.
 * @param separatorChar (theme string: "progressbar.separator") The character to draw in between the competed and pending bar in the active state.
 * @param completeChar (theme string: "progressbar.complete") The character to use to draw the completed portion of the bar in the active state.
 * @param pendingStyle(theme style: "progressbar.pending") The style to use for the [pendingChar]s
 * @param separatorStyle (theme style: "progressbar.separator") The style to use for the [separatorChar]
 * @param completeStyle (theme style: "progressbar.complete") The style to use for the [completeChar] when completed < total
 * @param finishedStyle (theme style: "progressbar.complete") The style to use for the [completeChar] when total <= completed
 * @param indeterminateStyle e (theme style: "progressbar.separator") The style to use when the state us indeterminate
 * @param showPulse (theme flag: "progressbar.pulse") If false, never draw the pulse animation in the indeterminate state.
 * @param fps The number of times per second to update the displayed progress. 30 by default.
 */
fun ProgressBarBuilder<*>.progressBar(
    width: Int? = null,
    pendingChar: String? = null,
    separatorChar: String? = null,
    completeChar: String? = null,
    pendingStyle: TextStyle? = null,
    separatorStyle: TextStyle? = null,
    completeStyle: TextStyle? = null,
    finishedStyle: TextStyle? = null,
    indeterminateStyle: TextStyle? = null,
    showPulse: Boolean? = null, // TODO replace with `pulseDuration: Duration?`
    fps: Int = 30,
) = cell(
    width?.let { ColumnWidth.Fixed(it) } ?: ColumnWidth.Expand(),
    fps = fps
) {
    val period = 2 // this could be configurable
    val pulsePosition = ((elapsed.toDouble(DurationUnit.SECONDS) % period) / period)

    ProgressBar(
        total,
        completed,
        isIndeterminate,
        width,
        pulsePosition.toFloat(),
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
}
