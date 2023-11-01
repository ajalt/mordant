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

// TODO copy docstrings
fun ProgressBarBuilder<*>.text(content: String, align: TextAlign? = null) {
    cell(align = align) { Text(content) }
}

fun <T> ProgressBarBuilder<T>.text(
    align: TextAlign? = null,
    content: ProgressState<T>.() -> String,
) {
    cell(align = align) { Text(content()) }
}

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

//TODO: timeElapsed
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

fun ProgressBarBuilder<*>.spinner(spinner: Spinner, fps: Int = 8) = cell(fps = fps) {
    spinner.tick = (elapsed.toDouble(DurationUnit.SECONDS) / fps).toInt()
    if (isFinished) BlankWidgetWrapper(spinner)
    else spinner
}

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
