package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.components.Padding
import com.github.ajalt.mordant.components.Text
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.table.grid
import com.github.ajalt.mordant.terminal.Terminal
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private const val SI_PREFIXES = "KMGTEPZY"

internal abstract class ProgressCell(
    val columnWidth: ColumnWidth,
    protected var renderable: Renderable = EmptyRenderable
) : Renderable {
    abstract fun update(total: Int, indeterminate: Boolean, frame: Int, hz: Int, history: ProgressHistory)
    override fun measure(t: Terminal, width: Int): WidthRange = renderable.measure(t, width)
    override fun render(t: Terminal, width: Int): Lines = renderable.render(t, width)
}

internal class TextProgressCell(text: Text) : ProgressCell(ColumnWidth.Auto, text) {
    override fun update(total: Int, indeterminate: Boolean, frame: Int, hz: Int, history: ProgressHistory) {}
}

internal class PercentageProgressCell(private val style: TextStyle) : ProgressCell(ColumnWidth.Fixed(4)) {
    override fun update(total: Int, indeterminate: Boolean, frame: Int, hz: Int, history: ProgressHistory) {
        if (indeterminate) {
            renderable = Text("", style)
            return
        }

        renderable = Text("${(100 * history.completed / total)}%", style)
    }
}

internal class CompletedProgressCell(
    private val suffix: String,
    private val includeTotal: Boolean,
    private val style: TextStyle
) : ProgressCell(ColumnWidth.Fixed((if (includeTotal) 11 else 5) + suffix.length)) {
    override fun update(total: Int, indeterminate: Boolean, frame: Int, hz: Int, history: ProgressHistory) {
        if (indeterminate) {
            renderable = EmptyRenderable
            return
        }

        val complete = history.completed.toDouble()
        val t = if (includeTotal) {
            val (nums, unit) = formatFloats(1, complete, total.toDouble())
            "${nums[0]}/${nums[1]}$unit"
        } else {
            complete.format(1)
        } + suffix
        renderable = Text(t, style, whitespace = Whitespace.PRE)
    }
}

internal abstract class ThrottledProgressCell(
    frameRate: Int?,
    columnWidth: ColumnWidth,
    renderable: Renderable = EmptyRenderable
) : ProgressCell(columnWidth, renderable) {
    private var lastFrameTime = 0.0
    private val frameDuration = frameRate?.let { 1.0 / it }
    protected fun shouldSkipUpdate(frame: Int, elapsed: Double): Boolean {
        if (frameDuration == null || frame == 0) return false
        if ((elapsed - lastFrameTime) < frameDuration) return true
        lastFrameTime = elapsed
        return false
    }
}

internal class SpeedProgressCell(
    private val suffix: String,
    frameRate: Int?,
    private val style: TextStyle
) : ThrottledProgressCell(frameRate, ColumnWidth.Fixed(5 + suffix.length)) {
    override fun update(total: Int, indeterminate: Boolean, frame: Int, hz: Int, history: ProgressHistory) {
        val speed = history.completedPerSecond ?: return
        if (history.completed == 0 || shouldSkipUpdate(frame, history.elapsed)) return

        if (indeterminate) {
            renderable = Text("--.--$suffix", style)
            return
        }
        renderable = Text(speed.format(1) + suffix, style, whitespace = Whitespace.PRE)
    }
}

internal class EtaProgressCell(
    private val prefix: String,
    frameRate: Int?,
    private val style: TextStyle,
) : ThrottledProgressCell(frameRate, ColumnWidth.Fixed(7 + prefix.length)) {
    override fun update(total: Int, indeterminate: Boolean, frame: Int, hz: Int, history: ProgressHistory) {
        val speed = history.completedPerSecond ?: return
        if (shouldSkipUpdate(frame, history.elapsed)) return

        val eta = (total - history.completed) / speed

        if (indeterminate || eta < 0 || speed <= 0) {
            renderable = Text("$prefix-:--:--", style, whitespace = Whitespace.PRE)
            return
        }

        val h = (eta / (60 * 60)).roundToInt()
        val m = (eta / 60 % 60).roundToInt()
        val s = (eta % 60).roundToInt().coerceAtLeast(if (h == 0 && m == 0) 1 else 0)

        renderable = Text(
            "$prefix$h:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}",
            style,
            whitespace = Whitespace.PRE
        )
    }
}

// TODO arguments to ProgressBar constructor
internal class BarProgressCell(val width: Int?) : ProgressCell(
    columnWidth = width?.let { ColumnWidth.Fixed(it) } ?: ColumnWidth.Expand(),
    renderable = ProgressBar(width = width)
) {
    override fun update(total: Int, indeterminate: Boolean, frame: Int, hz: Int, history: ProgressHistory) {
        renderable = ProgressBar(total, history.completed, indeterminate, width, pulseFrame = frame, pulseDuration = hz)
    }
}

private fun formatFloats(decimals: Int, vararg nums: Double): Pair<List<String>, String> {
    var n = nums.maxOrNull()!!
    var suffix = ""
    for (c in SI_PREFIXES) {
        if (n < 1000) {
            break
        }
        n /= 1000
        for (i in nums.indices) {
            nums[i] = nums[i] / 1000
        }
        suffix = c.toString()
    }
    return nums.map { num ->
        val s = num.toString()
        val len = s.indexOf('.').let { if (it >= 0) it + decimals + 1 else s.length }
        s.take(len)
    } to suffix
}

private fun Double.format(decimals: Int): String {
    return formatFloats(decimals, this).let { it.first.first() + it.second }
}

private fun nanosToSeconds(nanos: Double) = nanos / TimeUnit.SECONDS.toNanos(1)
private fun nanosToSeconds(nanos: Long) = nanosToSeconds(nanos.toDouble())

private class ProgressHistoryEntry(val timeNs: Long, val completed: Int)
internal class ProgressHistory(lengthSeconds: Float) {
    private val samples = ArrayDeque<ProgressHistoryEntry>()
    private val lengthNs = (TimeUnit.SECONDS.toNanos(1) * lengthSeconds).toLong()

    fun clear() {
        samples.clear()
    }

    fun reset() {
        clear()
    }

    fun update(completed: Int) {
        val now = System.nanoTime()
        val keepTime = now - lengthNs
        while (samples.firstOrNull().let { it != null && it.timeNs < keepTime }) {
            samples.removeFirst()
        }
        samples.addLast(ProgressHistoryEntry(now, completed))
    }

    val completed: Int
        get() = samples.lastOrNull()?.completed ?: 0

    val elapsed: Double
        get() {
            if (samples.size < 2) return 0.0
            return nanosToSeconds(samples.last().timeNs - samples.first().timeNs)
        }

    val completedPerSecond: Double?
        get() {
            val elapsed = elapsed
            if (elapsed <= 0) return null
            val complete = samples.last().completed - samples.first().completed
            return complete.toDouble() / elapsed
        }
}

// TODO: thread safety
class ProgressTracker internal constructor(
    t: Terminal,
    private val cells: List<ProgressCell>,
    private val frameRate: Int,
    historyLength: Float,
    private val ticker: Ticker,
    private val paddingSize: Int,
) {
    private var total: Int? = null
    private val history = ProgressHistory(historyLength)
    private var frame = 0
    private val animation = t.animation<Unit> {
        grid {
            rowFrom(cells)
            align = TextAlign.RIGHT
            borders = Borders.NONE
            cells.forEachIndexed { i, it ->
                column(i) {
                    width = it.columnWidth
                    padding = when (i) {
                        cells.lastIndex -> Padding.of(left = paddingSize)
                        else -> Padding.of(right = paddingSize)
                    }
                }
            }
        }
    }

    fun update(advancePulse: Boolean = true) {
        if (advancePulse) frame += 1
        cells.forEach {
            it.update(
                this.total ?: 100,
                this.total == null,
                frame,
                frameRate,
                history
            )
        }
        animation.update(Unit)
    }

    fun update(completed: Int) {
        history.update(completed)
        update()
    }

    fun update(completed: Int, total: Int?) {
        updateTotalWithoutAnimation(total)
        update(completed)
    }

    fun updateTotal(total: Int?) {
        updateTotalWithoutAnimation(total)
        update()
    }

    private fun updateTotalWithoutAnimation(total: Int?) {
        this.total = total?.takeIf { it > 0 }
    }

    fun advance(amount: Int) {
        update(history.completed + amount)
    }

    fun start() {
        ticker.start { update() }
    }

    fun stop() {
        ticker.stop()
    }

    fun restart() {
        stop()
        update(0)
        start()
    }

    fun clear() {
        stop()
        animation.clear()
    }
}
