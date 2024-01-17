package com.github.ajalt.mordant.widgets.progress

import com.github.ajalt.mordant.internal.*
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.VerticalAlign
import com.github.ajalt.mordant.rendering.Whitespace
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.widgets.ProgressBar
import com.github.ajalt.mordant.widgets.Spinner
import com.github.ajalt.mordant.widgets.Text
import com.github.ajalt.mordant.widgets.Viewport
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

/**
 * Add a fixed text cell to this layout.
 *
 * The cell is always the same size as the [content]. For a fixed-width text cell, use [marquee].
 *
 * @param content The text to display in this cell.
 * @param align The text alignment for this cell. Cells are right-aligned by default.
 */
fun ProgressLayoutScope<*>.text(
    content: String,
    align: TextAlign = this.align,
    verticalAlign: VerticalAlign = this.verticalAlign,
) {
    cell(fps = 0, align = align, verticalAlign = verticalAlign) { Text(content) }
}

/**
 * Add a dynamic text cell to this layout.
 *
 * The [content] lambda will be called with the current progress state as its receiver.
 *
 * The cell is always the same size as the [content]. For a fixed-width text cell, use [marquee].
 *
 * ### Example
 * ```
 * text { context.toString() }
 * ```
 *
 * @param align The text alignment for this cell. Cells are right-aligned by default.
 * @param content A lambda returning the text to display in this cell.
 */
fun <T> ProgressLayoutScope<T>.text(
    align: TextAlign = this.align,
    verticalAlign: VerticalAlign = this.verticalAlign,
    fps: Int = textFps,
    content: ProgressState<T>.() -> String,
) {
    cell(fps = fps, align = align, verticalAlign = verticalAlign) { Text(content()) }
}

/**
 * Add a fixed width text cell that scrolls its contents horizontally so that long text can be
 * displayed in a fixed width.
 *
 * @param width The width of the cell in characters.
 * @param fps The number of times per second to update the displayed text.
 * @param align The text alignment for this cell when [scrollWhenContentFits] is `false` and the
 *   [content] fits in the [width].
 * @param scrollWhenContentFits If `true`, the text will always scroll, even if it fits in the
 *   [width].
 * @param content The text to display in this cell.
 */
fun <T> ProgressLayoutScope<T>.marquee(
    width: Int,
    fps: Int = 3,
    align: TextAlign = this.align,
    verticalAlign: VerticalAlign = this.verticalAlign,
    scrollWhenContentFits: Boolean = false,
    content: ProgressState<T>.() -> String,
) {
    require(width > 0) { "width must be greater than zero" }
    cell(ColumnWidth.Fixed(width), fps, align, verticalAlign) {
        val text = content()
        val cellWidth = parseText(text, DEFAULT_STYLE).width
        when {
            !scrollWhenContentFits && cellWidth <= width -> Text(text)
            else -> {
                val period = cellWidth + width
                val scrollRight = if (isFinished) 0 else frameCount(fps) % period - width
                Viewport(Text(text), width, scrollRight = scrollRight)
            }
        }
    }
}

/**
 * Add a fixed width text cell that scrolls its contents horizontally so that long text can be
 * displayed in a fixed width.
 *
 * @param content The text to display in this cell.
 * @param width The width of the cell in characters.
 * @param fps The number of times per second to update the displayed text.
 * @param align The text alignment for this cell when [scrollWhenContentFits] is `false` and the
 *   [content] fits in the [width].
 * @param scrollWhenContentFits If `true`, the text will always scroll, even if it fits in the
 *   [width].
 */
fun ProgressLayoutScope<*>.marquee(
    content: String,
    width: Int,
    fps: Int = 3,
    align: TextAlign = this.align,
    verticalAlign: VerticalAlign = this.verticalAlign,
    scrollWhenContentFits: Boolean = false,
) = marquee(width, fps, align, verticalAlign, scrollWhenContentFits) { content }

/**
 * Add a cell that displays the current completed count to this layout.
 *
 * @param suffix A string to append to the end of the displayed count, such as "B" if you are tracking bytes. Empty by default.
 * @param includeTotal If true, the total count will be displayed after the completed count, separated by a slash. True by default.
 * @param precision The number of decimal places to display. 1 by default.
 * @param style The style to use for the displayed count.
 * @param fps The number of times per second to update the displayed count. Uses the
 *  [text fps][ProgressLayoutScope.textFps] by default.
 */
fun ProgressLayoutScope<*>.completed(
    suffix: String = "",
    includeTotal: Boolean = true,
    precision: Int = 1,
    style: TextStyle = DEFAULT_STYLE,
    verticalAlign: VerticalAlign = this.verticalAlign,
    fps: Int = textFps,
) = cell(
    // "1000"
    // "100.0M"
    // "100.0/200.0M"
    width = ColumnWidth.Fixed(
        (3 + precision + if (precision > 0) 1 else 0).let {
            (if (includeTotal) it * 2 + 1 else it) + suffix.length + 1
        }
    ),
    fps = fps,
    verticalAlign = verticalAlign,
) {
    val complete = completed.toDouble()
    val total = total?.toDouble()
    val (nums, unit) = formatMultipleWithSiSuffixes(precision, true, complete, total ?: 0.0)

    val formattedTotal = when {
        includeTotal && total != null && total >= 0 -> "/${nums[1]}$unit"
        includeTotal && (total == null || total < 0) -> "/---.-"
        else -> ""
    }
    Text(style(nums[0] + formattedTotal + suffix), whitespace = Whitespace.PRE)
}

/**
 * Add a cell that displays the current speed to this layout.
 *
 * @param suffix A string to append to the end of the displayed speed, such as "B/s" if you are tracking bytes. "/s" by default.
 * @param style The style to use for the displayed speed.
 * @param fps The number of times per second to update the displayed speed. Uses the
 *  [text fps][ProgressLayoutScope.textFps] by default.
 */
fun ProgressLayoutScope<*>.speed(
    suffix: String = "/s",
    style: TextStyle = DEFAULT_STYLE,
    verticalAlign: VerticalAlign = this.verticalAlign,
    fps: Int = textFps,
) = cell(
    ColumnWidth.Fixed(6 + suffix.length), // " 100.0M"
    fps = fps,
    verticalAlign = verticalAlign,
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
 * @param fps The number of times per second to update the displayed percentage. Uses the
 *  [text fps][ProgressLayoutScope.textFps] by default.
 */
fun ProgressLayoutScope<*>.percentage(
    fps: Int = textFps,
    style: TextStyle = DEFAULT_STYLE,
    verticalAlign: VerticalAlign = this.verticalAlign,
) = cell(
    ColumnWidth.Fixed(4),  // " 100%"
    fps = fps,
    verticalAlign = verticalAlign,
) {
    val percent = when {
        total == null || total <= 0 -> 0
        else -> (100.0 * completed / total).toInt()
    }
    Text(style("$percent%"))
}

/**
 * Add a cell that displays the time remaining to this layout.
 *
 * @param prefix A string to prepend to the displayed time, such as `"eta "` or `"time left: "`. `"eta "` by default.
 * @param compact If `true`, the displayed time will be formatted as `"MM:SS"` if time remaining is less than an hour. `false` by default.
 * @param elapsedWhenFinished If `true`, the elapsed time will be displayed when the task is finished. `false` by default.
 * @param elapsedPrefix A string to prepend to the displayed time when [elapsedWhenFinished] is `true`. `" in "` by default.
 * @param style The style to use for the displayed time.
 * @param fps The number of times per second to update the displayed time. Uses the
 *  [text fps][ProgressLayoutScope.textFps] by default.
 */
fun ProgressLayoutScope<*>.timeRemaining(
    prefix: String = "eta ",
    compact: Boolean = false,
    elapsedWhenFinished: Boolean = false,
    elapsedPrefix: String = " in ",
    style: TextStyle = DEFAULT_STYLE,
    verticalAlign: VerticalAlign = this.verticalAlign,
    fps: Int = textFps,
) = cell(
    ColumnWidth.Fixed(7 + max(prefix.length, elapsedPrefix.length)), // "0:00:02"
    fps = fps,
    verticalAlign = verticalAlign,
) {
    val eta = when {
        status is ProgressState.Status.Finished && elapsedWhenFinished -> {
            status.finishTime - status.startTime
        }

        status is ProgressState.Status.Running && speed != null && total != null -> {
            ((total - completed) / speed).seconds
        }

        else -> null
    }
    val p = if (isFinished && elapsedWhenFinished) elapsedPrefix else prefix
    val maxEta = 35_999.seconds // 9:59:59
    val duration = if (eta != null && eta <= maxEta) eta else null
    Text(style(p + renderDuration(duration, compact)), whitespace = Whitespace.PRE)
}

/**
 * Add a cell that displays the elapsed time to this layout.
 *
 * @param compact If `true`, the displayed time will be formatted as "MM:SS" if time remaining is less than an hour. `false` by default.
 * @param style The style to use for the displayed time.
 * @param fps The number of times per second to update the displayed time. Uses the
 *  [text fps][ProgressLayoutScope.textFps] by default.
 */
fun ProgressLayoutScope<*>.timeElapsed(
    compact: Boolean = false,
    style: TextStyle = DEFAULT_STYLE,
    verticalAlign: VerticalAlign = this.verticalAlign,
    fps: Int = textFps,
) = cell(ColumnWidth.Auto, fps = fps, verticalAlign = verticalAlign) {
    val elapsed = when (status) {
        ProgressState.Status.NotStarted -> null
        is ProgressState.Status.Finished -> status.finishTime - status.startTime
        is ProgressState.Status.Paused -> status.pauseTime - status.startTime
        is ProgressState.Status.Running -> status.startTime.elapsedNow()
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
fun ProgressLayoutScope<*>.spinner(
    spinner: Spinner,
    verticalAlign: VerticalAlign = this.verticalAlign,
    fps: Int = 8,
) = cell(fps = fps, verticalAlign = verticalAlign) {
    spinner.tick = frameCount(fps)
    if (isRunning) spinner else BlankWidgetWrapper(spinner)
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
 * @param fps The number of times per second to update the displayed bar.  Uses the
 *  [animation fps][ProgressLayoutScope.animationFps] by default.
 */
fun ProgressLayoutScope<*>.progressBar(
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
    verticalAlign: VerticalAlign = this.verticalAlign,
    fps: Int = animationFps,
) = cell(
    width?.let { ColumnWidth.Fixed(it) } ?: ColumnWidth.Expand(),
    fps = fps,
    verticalAlign = verticalAlign,
) {
    val period = 2 // this could be configurable
    val elapsedSeconds = animationTime.elapsedNow().toDouble(DurationUnit.SECONDS)
    val pulsePosition = ((elapsedSeconds % period) / period)

    ProgressBar(
        total = total ?: 0,
        completed = completed,
        indeterminate = isIndeterminate,
        width = width,
        pulsePosition = pulsePosition.toFloat(),
        showPulse = if (isRunning) showPulse else false,
        pendingChar = pendingChar,
        separatorChar = separatorChar,
        completeChar = completeChar,
        pendingStyle = pendingStyle,
        separatorStyle = separatorStyle,
        completeStyle = completeStyle,
        finishedStyle = finishedStyle,
        indeterminateStyle = indeterminateStyle
    )
}

private fun renderDuration(duration: Duration?, compact: Boolean): String {
    if (duration == null || duration < Duration.ZERO) {
        return if (compact) "--:--" else "-:--:--"
    }
    return duration.toComponents { h, m, s, _ ->
        val hrs = if (compact && h <= 0) "" else "$h:"
        "$hrs${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    }
}
