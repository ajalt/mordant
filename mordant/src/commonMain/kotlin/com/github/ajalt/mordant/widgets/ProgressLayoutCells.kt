package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.BlankWidgetWrapper
import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.formatMultipleWithSiSuffixes
import com.github.ajalt.mordant.internal.formatWithSiSuffix
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.Whitespace
import com.github.ajalt.mordant.table.ColumnWidth
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
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
    val total = total?.toDouble()
    val (nums, unit) = formatMultipleWithSiSuffixes(1, complete, total ?: 0.0)

    val formattedTotal = when {
        includeTotal && total != null && total >= 0 -> "/${nums[1]}$unit"
        includeTotal && (total == null || total < 0) -> "/---.-"
        else -> ""
    }
    Text(style(nums[0] + formattedTotal + suffix), whitespace = Whitespace.PRE)
}

//TODO: change suffix to "/s"?
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
        speed == null || speed < 0 -> "---.-"
        else -> speed.formatWithSiSuffix(1)
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
        total == null || total <= 0 -> 0
        else -> (100.0 * completed / total).toInt()
    }
    Text("$percent%")
}

// TODO: add an option to show elapsed time once the task is finished
/**
 * Add a cell that displays the time remaining to this layout.
 *
 * @param prefix A string to prepend to the displayed time, such as "eta " or "time left: ". "eta " by default.
 * @param compact If true, the displayed time will be formatted as "MM:SS" if time remaining is less than an hour. False by default.
 * @param style The style to use for the displayed time.
 * @param fps The number of times per second to update the displayed time. 5 by default.
 */
fun ProgressBarBuilder<*>.timeRemaining(
    prefix: String = "eta ",
    compact: Boolean = false,
    style: TextStyle = DEFAULT_STYLE,
    fps: Int = 5,
) = cell(
    ColumnWidth.Fixed(7 + prefix.length), // " 0:00:02"
    fps = fps
) {
    val eta = when {
        isFinished || isPaused || speed == null || total == null -> null
        else -> (total - completed) / speed
    }
    val maxEta = 35_999 // 9:59:59
    val duration = if (eta != null && eta <= maxEta) eta.seconds else null
    Text(style(prefix + renderDuration(duration, compact)), whitespace = Whitespace.PRE)
}


/**
 * Add a cell that displays the elapsed time to this layout.
 *
 * @param compact If true, the displayed time will be formatted as "MM:SS" if time remaining is less than an hour. False by default.
 * @param style The style to use for the displayed time.
 * @param fps The number of times per second to update the displayed time. 5 by default.
 */
fun ProgressBarBuilder<*>.timeElapsed(
    compact: Boolean = false,
    style: TextStyle = DEFAULT_STYLE,
    fps: Int = 5,
) = cell(ColumnWidth.Auto, fps = fps) {
    val elapsed = when {
        finishedTime != null && startedTime != null -> finishedTime - startedTime // TODO: test this
        else -> animationTime.elapsedNow()
    }
    Text(style(renderDuration(elapsed, compact)), whitespace = Whitespace.PRE)
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
    spinner.tick = (animationTime.elapsedNow().toDouble(DurationUnit.SECONDS) / fps).toInt()
    if (isPaused) BlankWidgetWrapper(spinner)
    else spinner
}

/**
 * Add a progress bar cell to this layout.
 *
 * @param width The width in characters for this widget, or `null` to expand to fill the remaining space.
 * @param pendingChar (theme string: "progressbar.pending") The character to use to draw the pending portion of the bar in the active state.
 * @param separatorChar (theme string: "progressbar.separator") The character to draw in between the competed and pending bar in the active state.
 * @param completeChar (theme string: "progressbar.complete") The character to use to draw the completed portion of the bar in the active state.
 * @param pendingStyle(theme style: "progressbar.pending") The style to use for the [pendingChar]s
 * @param separatorStyle (theme style: "progressbar.separator") The style to use for the [separatorChar]
 * @param completeStyle (theme style: "progressbar.complete") The style to use for the [completeChar] when completed < total
 * @param finishedStyle (theme style: "progressbar.complete") The style to use for the [completeChar] when total <= completed
 * @param indeterminateStyle e (theme style: "progressbar.separator") The style to use when the state us indeterminate
 * @param showPulse (theme flag: "progressbar.pulse") If false, never draw the pulse animation in the indeterminate state.
 * @param fps The number of times per second to update the displayed bar. 30 by default.
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
    val elapsedSeconds = animationTime.elapsedNow().toDouble(DurationUnit.SECONDS)
    val pulsePosition = ((elapsedSeconds % period) / period)

    ProgressBar(
        total ?: 0,
        completed,
        isIndeterminate,
        width,
        pulsePosition.toFloat(),
        if (isStarted) showPulse else false,
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

private fun renderDuration(duration: Duration?, comapct: Boolean): String {
    if (duration == null || duration < Duration.ZERO) return "-:--:--"
    return duration.toComponents { h, m, s, _ ->
        val hrs = if (comapct && h <= 0) "" else "$h:"
        "$hrs${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    }
}
