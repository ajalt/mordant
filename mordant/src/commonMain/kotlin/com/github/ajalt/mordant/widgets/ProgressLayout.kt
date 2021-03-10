package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.table.grid

open class ProgressBuilder internal constructor() {
    var padding: Int = 2

    fun text(text: String) {
        cells += TextProgressCell(Text(text))
    }

    fun percentage() {
        cells += PercentageProgressCell()
    }

    fun progressBar(
        width: Int? = null,
        pendingChar: String? = null,
        separatorChar: String? = null,
        completeChar: String? = null,
        pendingStyle: TextStyle? = null,
        separatorStyle: TextStyle? = null,
        completeStyle: TextStyle? = null,
        finishedStyle: TextStyle? = null,
        indeterminateStyle: TextStyle? = null,
    ) {
        cells += BarProgressCell(
            width,
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

    fun completed(suffix: String = "", includeTotal: Boolean = true, style: TextStyle = DEFAULT_STYLE) {
        cells += CompletedProgressCell(suffix, includeTotal, style)
    }

    fun speed(suffix: String = "it/s", style: TextStyle = DEFAULT_STYLE) {
        cells += SpeedProgressCell(suffix, style)
    }

    fun timeRemaining(prefix: String = "eta ", style: TextStyle = DEFAULT_STYLE) {
        cells += EtaProgressCell(prefix, style)
    }

    internal fun build(): ProgressLayout {
        return ProgressLayout(cells, padding)
    }

    internal val cells = mutableListOf<ProgressCell>()
}

class ProgressLayout internal constructor(
    internal val cells: List<ProgressCell>,
    private val paddingSize: Int,
) {
    fun build(
        completed: Long,
        total: Long? = null,
        elapsedSeconds: Double = 0.0,
        completedPerSecond: Double? = null,
    ): Widget {
        val cps = completedPerSecond ?: when {
            completed <= 0 || elapsedSeconds <= 0 -> 0.0
            else -> completed.toDouble() / elapsedSeconds
        }
        val state = ProgressState(
            completed = completed,
            total = total,
            completedPerSecond = cps,
            elapsedSeconds = elapsedSeconds,
        )
        return grid {
            rowFrom(cells.map { it.run { state.run { makeWidget() } } })
            align = TextAlign.RIGHT
            borders = Borders.NONE
            cells.forEachIndexed { i, it ->
                column(i) {
                    padding = when (i) {
                        cells.lastIndex -> Padding.none()
                        else -> Padding.of(right = paddingSize)
                    }
                    // Expand fixed columns to account for padding
                    width = when (i) {
                        cells.lastIndex -> it.columnWidth
                        else -> (it.columnWidth as? ColumnWidth.Fixed)
                            ?.let { ColumnWidth.Fixed(it.width + paddingSize) }
                            ?: it.columnWidth
                    }
                }
            }
        }
    }
}

fun progressLayout(init: ProgressBuilder.() -> Unit): ProgressLayout {
    return ProgressBuilder().apply(init).build()
}
