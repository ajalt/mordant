package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.formatMultipleWithSiSuffixes
import com.github.ajalt.mordant.internal.formatWithSiSuffix
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.Whitespace
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.widgets.ProgressCell.AnimationRate
import kotlin.math.roundToInt


internal data class ProgressState(
    val completed: Long,
    val total: Long?,
    val completedPerSecond: Double, // 0 if [completed] == 0
    val elapsedSeconds: Double,
) {
    init {
        require(completed >= 0) { "completed cannot be negative" }
        require(total == null || total >= 0) { "total cannot be negative" }
        require(elapsedSeconds >= 0) { "elapsedSeconds cannot be negative" }
        require(completedPerSecond >= 0) { "completedPerSecond cannot be negative" }
    }

    val indeterminate: Boolean get() = total == null
}

internal interface ProgressCell {
    enum class AnimationRate {
        STATIC, ANIMATION, TEXT
    }

    val columnWidth: ColumnWidth
    val animationRate: AnimationRate

    fun ProgressState.makeWidget(): Widget
}


internal class TextProgressCell(private val text: Text) : ProgressCell {
    override val animationRate: AnimationRate get() = AnimationRate.STATIC
    override val columnWidth: ColumnWidth get() = ColumnWidth.Auto
    override fun ProgressState.makeWidget(): Widget = text
}

internal class PercentageProgressCell : ProgressCell {
    override val animationRate: AnimationRate get() = AnimationRate.TEXT
    override val columnWidth: ColumnWidth get() = ColumnWidth.Fixed(4)
    override fun ProgressState.makeWidget(): Widget {
        val percent = when {
            total == null || total <= 0 -> 0
            else -> (100.0 * completed / total).toInt()
        }
        return Text("$percent%")
    }
}

internal class CompletedProgressCell(
    private val suffix: String,
    private val includeTotal: Boolean,
    private val style: TextStyle,
) : ProgressCell {
    override val animationRate: AnimationRate get() = AnimationRate.TEXT
    override val columnWidth: ColumnWidth
        get() = ColumnWidth.Fixed((if (includeTotal) 12 else 6) + suffix.length)

    override fun ProgressState.makeWidget(): Widget {
        val complete = completed.toDouble()
        val (nums, unit) = formatMultipleWithSiSuffixes(1, complete, total?.toDouble() ?: 0.0)

        val t = nums[0] + when {
            includeTotal && total != null -> "/${nums[1]}$unit"
            includeTotal && total == null -> "/---.-"
            else -> ""
        } + suffix
        return Text(style(t), whitespace = Whitespace.PRE)
    }
}

internal class SpeedProgressCell(
    private val suffix: String,
    private val style: TextStyle,
) : ProgressCell {
    override val animationRate: AnimationRate get() = AnimationRate.TEXT
    override val columnWidth: ColumnWidth get() = ColumnWidth.Fixed(6 + suffix.length)

    override fun ProgressState.makeWidget(): Widget {
        val t = when {
            indeterminate || completedPerSecond <= 0 -> "---.-"
            else -> completedPerSecond.formatWithSiSuffix(1)
        }
        return Text(style(t + suffix), whitespace = Whitespace.PRE)
    }
}

internal class EtaProgressCell(
    private val prefix: String,
    private val style: TextStyle,
) : ProgressCell {
    override val animationRate: AnimationRate get() = AnimationRate.TEXT
    override val columnWidth: ColumnWidth get() = ColumnWidth.Fixed(7 + prefix.length)

    override fun ProgressState.makeWidget(): Widget {
        val eta = if (total == null) 0.0 else (total - completed) / completedPerSecond
        val maxEta = 35_999 // 9:59:59
        if (indeterminate || eta < 0 || completedPerSecond == 0.0 || eta > maxEta) {
            return text("$prefix-:--:--")
        }

        val h = (eta / (60 * 60)).toInt()
        val m = (eta / 60 % 60).toInt()
        val s = (eta % 60).roundToInt()

        return text("$prefix$h:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}")
    }

    private fun text(s: String) = Text(style(s), whitespace = Whitespace.PRE)
}

internal class BarProgressCell(
    val width: Int?,
    private val pendingChar: String? = null,
    private val separatorChar: String? = null,
    private val completeChar: String? = null,
    private val pendingStyle: TextStyle? = null,
    private val separatorStyle: TextStyle? = null,
    private val completeStyle: TextStyle? = null,
    private val finishedStyle: TextStyle? = null,
    private val indeterminateStyle: TextStyle? = null,
    private val pulse: Boolean? = null,
) : ProgressCell {
    override val animationRate: AnimationRate get() = AnimationRate.ANIMATION
    override val columnWidth: ColumnWidth
        get() = width?.let { ColumnWidth.Fixed(it) } ?: ColumnWidth.Expand()

    override fun ProgressState.makeWidget(): Widget {
        val period = 2 // this could be configurable
        val pulsePosition = ((elapsedSeconds % period) / period)

        return ProgressBar(
            total ?: 100,
            completed,
            indeterminate,
            width,
            pulsePosition.toFloat(),
            pulse,
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
}
