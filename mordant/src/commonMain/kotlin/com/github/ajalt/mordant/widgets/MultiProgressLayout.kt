package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.BlankWidgetWrapper
import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.formatMultipleWithSiSuffixes
import com.github.ajalt.mordant.internal.formatWithSiSuffix
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.Whitespace
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.table.table
import kotlin.math.roundToInt


// TODO maybe...
//  sealed class ProgressCompletion {
//      object Indeterminate : ProgressCompletion()
//      data class Started(val completed: Long, val total: Long) : ProgressCompletion()
//  }

data class ProgressTask<T>(
    var context: T,
    var completed: Long,
    var total: Long?,
) {
    val isIndeterminate: Boolean get() = total == null
    val isFinished: Boolean get() = total?.let { completed >= it } ?: false
}

class ProgressCellBuilder<T>(
    val elapsedSeconds: Double,
    val task: ProgressTask<T>,
    completedPerSecond: Double? = null,
) {
    val completedPerSecond: Double = completedPerSecond ?: when {
        task.completed <= 0 || elapsedSeconds <= 0 -> 0.0
        else -> task.completed / elapsedSeconds
    }
}

// TODO: make private once builder.cells isn't public
internal data class NewProgressCell<T>(
    val columnWidth: ColumnWidth,
    private val fps: Int,
    private val builder: ProgressCellBuilder<T>.() -> Widget,
) {
    private var lastFrame: Widget? = null
    private val lastFrameTime: Double = Double.MIN_VALUE

    fun buildFrame(task: ProgressTask<T>, elapsed: Double, completedPerSecond: Double?): Widget {
        if (lastFrame != null && (fps <= 0 || (elapsed - lastFrameTime) < 1.0 / fps)) {
            return lastFrame!!
        }
        return builder(ProgressCellBuilder(elapsed, task, completedPerSecond)).also {
            lastFrame = it
        }
    }

}

class NewProgressBuilder<T> {
    var spacing: Int = 2

    // TODO: could make a fun LinearLayout.addToLayout instead
    internal val cells: MutableList<NewProgressCell<T>> = mutableListOf()

    fun cell(
        width: ColumnWidth = ColumnWidth.Auto,
        fps: Int = 0,
        builder: ProgressCellBuilder<T>.() -> Widget,
    ) {
        cells += NewProgressCell(width, fps, builder)
    }
}

// TODO copy docstrings
fun NewProgressBuilder<*>.text(content: String) {
    cell { Text(content) }
}

fun <T> NewProgressBuilder<T>.text(content: ProgressCellBuilder<T>.() -> String) {
    cell { Text(content()) }
}

fun NewProgressBuilder<*>.completed(
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
    val complete = task.completed.toDouble()
    val total = task.total
    val (nums, unit) = formatMultipleWithSiSuffixes(1, complete, total?.toDouble() ?: 0.0)

    val t = nums[0] + when {
        includeTotal && total != null -> "/${nums[1]}$unit"
        includeTotal && total == null -> "/---.-"
        else -> ""
    } + suffix
    Text(style(t), whitespace = Whitespace.PRE)
}

fun NewProgressBuilder<*>.speed(
    suffix: String = "it/s",
    style: TextStyle = DEFAULT_STYLE,
    fps: Int = 5,
) = cell(
    ColumnWidth.Fixed(6 + suffix.length), // " 100.0M"
    fps = fps
) {
    val t = when {
        task.isIndeterminate || completedPerSecond <= 0 -> "---.-"
        else -> completedPerSecond.formatWithSiSuffix(1)
    }
    Text(style(t + suffix), whitespace = Whitespace.PRE)
}

fun NewProgressBuilder<*>.percentage(fps: Int = 5) = cell(
    ColumnWidth.Fixed(4),  // " 100%"
    fps = fps
) {
    val total = task.total
    val percent = when {
        total == null || total <= 0 -> 0
        else -> (100.0 * task.completed / total).toInt()
    }
    Text("$percent%")
}

fun NewProgressBuilder<*>.timeRemaining(
    prefix: String = "eta ",
    style: TextStyle = DEFAULT_STYLE,
    fps: Int = 5,
) = cell(
    ColumnWidth.Fixed(7 + prefix.length), // " 0:00:02"
    fps = fps
) {
    fun widget(s: String) = Text(style(s), whitespace = Whitespace.PRE)

    val total = task.total
    val eta = if (total == null) 0.0 else (total - task.completed) / completedPerSecond
    val maxEta = 35_999 // 9:59:59
    if (task.isIndeterminate || eta < 0 || completedPerSecond == 0.0 || eta > maxEta) {
        return@cell widget("$prefix-:--:--")
    }

    val h = (eta / (60 * 60)).toInt()
    val m = (eta / 60 % 60).toInt()
    val s = (eta % 60).roundToInt()

    widget("$prefix$h:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}")
}

fun NewProgressBuilder<*>.spinner(spinner: Spinner, fps: Int = 8) = cell(fps = fps) {
    spinner.tick = (elapsedSeconds / fps).toInt()
    if (task.isFinished) BlankWidgetWrapper(spinner)
    else spinner
}

fun NewProgressBuilder<*>.progressBar(
    width: Int? = null,
    pendingChar: String? = null,
    separatorChar: String? = null,
    completeChar: String? = null,
    pendingStyle: TextStyle? = null,
    separatorStyle: TextStyle? = null,
    completeStyle: TextStyle? = null,
    finishedStyle: TextStyle? = null,
    indeterminateStyle: TextStyle? = null,
    showPulse: Boolean? = null,
    fps: Int = 30,
) = cell(
    width?.let { ColumnWidth.Fixed(it) } ?: ColumnWidth.Expand(),
    fps = fps
) {
    val period = 2 // this could be configurable
    val pulsePosition = ((elapsedSeconds % period) / period)

    ProgressBar(
        task.total ?: 100,
        task.completed,
        task.isIndeterminate,
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

class ProgressFactory<T>(private val builder: NewProgressBuilder<T>) {
    // TODO var alignColumns: Boolean = true
    /** Amount of horizontal space between cells */

    private val tasks = mutableListOf<ProgressTask<T>>()

    fun addTask(context: T): ProgressTask<T> {
        return ProgressTask(context, 0, null).also { tasks.add(it) }
    }

    fun build(elapsedSeconds: Double, completedPerSecond: Double? = null): Widget {
        return table {
//          TODO  verticalAlign = verticalAlign
            cellBorders = Borders.NONE
            // TODO: fix spacing (see HorizontalLayoutBuilderInstance.build)
            padding = Padding { horizontal = builder.spacing }
            builder.cells.forEachIndexed { i, it ->
                align = TextAlign.RIGHT

                column(i) {
                    if (i > 0) {
                        padding = Padding { left = builder.spacing }
                    }
                    width = when (val w = it.columnWidth) {
                        // The fixed width cells don't include padding, so add it here
                        is ColumnWidth.Fixed -> ColumnWidth.Fixed(w.width + builder.spacing)
                        else -> w
                    }
                }
            }
            body {
                for (task in tasks) {
                    rowFrom(builder.cells.map {
                        it.buildFrame(task, elapsedSeconds, completedPerSecond)
                    })
                }
            }
        }
    }
}

// TODO: make a templated version or smth
fun multiProgressLayout(init: NewProgressBuilder<Unit>.() -> Unit): ProgressFactory<Unit> {
    return ProgressFactory(NewProgressBuilder<Unit>().apply(init))
}
