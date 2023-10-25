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

internal data class NewProgressCell<T>(
    val columnWidth: ColumnWidth,
    internal var fps: Int, // XXX: mutable for backcompat; this should be a private val
    private val builder: ProgressCellBuilder<T>.() -> Widget,
) {
    private var lastFrame: Widget? = null
    private var lastFrameTime: Double = Double.MIN_VALUE

    // TODO: use duration instead of elapsedSeconds
    fun buildFrame(task: ProgressTask<T>, elapsed: Double, completedPerSecond: Double?): Widget {
        val timeSinceLastFrame = elapsed - lastFrameTime
        val lastFrame = this.lastFrame
        val timePerFrame = 1.0 / fps
        if (lastFrame != null && (fps <= 0 || timeSinceLastFrame < timePerFrame)) {
            return lastFrame
        }
        lastFrameTime = elapsed
        return builder(ProgressCellBuilder(elapsed, task, completedPerSecond)).also {
            this.lastFrame = it
        }
    }

    fun reset() {
        lastFrame = null
        lastFrameTime = Double.MIN_VALUE
    }
}

// TODO: make NewProgressBuilder an interface?
class NewProgressBuilder<T> {
    /** Amount of horizontal space between cells */
    var spacing: Int = 2

    fun cell(
        width: ColumnWidth = ColumnWidth.Auto,
        fps: Int = 0,
        builder: ProgressCellBuilder<T>.() -> Widget,
    ) {
        cells += NewProgressCell(width, fps, builder)
    }

    // XXX: these are a hack for backcompat only
    internal var animationFrameRate: Int? = null
    internal var textFrameRate: Int? = null

    private val cells: MutableList<NewProgressCell<T>> = mutableListOf()

    internal fun build(): ProgressFactory<T> {
        // XXX: This is a hack for backcompat; if a global rate was set, apply it to all cells
        val animRate = animationFrameRate
        val textRate = textFrameRate
        for (cell in cells) {
            if (animRate != null && cell.fps == 30) cell.fps = animRate
            if (textRate != null && cell.fps == 5) cell.fps = textRate
        }
        return ProgressFactory(spacing, cells)
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

class ProgressFactory<T> internal constructor(
    private val spacing: Int,
    private val cells: List<NewProgressCell<T>>,
) {
    // TODO var alignColumns: Boolean = true (linear layouts vs table)

    private val tasks = mutableListOf<ProgressTask<T>>()

    fun addTask(context: T): ProgressTask<T> {
        return ProgressTask(context, 0, null).also { tasks.add(it) }
    }

    fun reset() {
        tasks.forEach { it.completed = 0 }
        cells.forEach { it.reset() }
    }

    fun build(elapsedSeconds: Double, completedPerSecond: Double? = null): Widget {
        return table {
//          TODO  verticalAlign = verticalAlign
            cellBorders = Borders.NONE
            padding = Padding { left = spacing }
            column(0) { padding = Padding(0) }
            cells.forEachIndexed { i, it ->
                align = TextAlign.RIGHT

                column(i) {
                    width = when (val w = it.columnWidth) {
                        // The fixed width cells don't include padding, so add it here
                        is ColumnWidth.Fixed -> ColumnWidth.Fixed(w.width + spacing)
                        else -> w
                    }
                }
            }
            body {
                for (task in tasks) {
                    rowFrom(cells.map {
                        it.buildFrame(task, elapsedSeconds, completedPerSecond)
                    })
                }
            }
        }
    }
}

// TODO: make a templated version or smth
fun multiProgressLayout(init: NewProgressBuilder<Unit>.() -> Unit): ProgressFactory<Unit> {
    return NewProgressBuilder<Unit>().apply(init).build()
}
