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

data class ProgressState(
    val completed: Int,
    val total: Int?,
    val completedPerSecond: Double, // 0 if [completed] == 0
    val elapsedSeconds: Double,
    val frameRate: Int,
) {
    init {
        require(completed >= 0) { "completed cannot be negative" }
        require(total == null || total >= 0) { "total cannot be negative" }
        require(elapsedSeconds >= 0) { "elapsedSeconds cannot be negative" }
        require(completedPerSecond >= 0) { "completedPerSecond cannot be negative" }
    }

    val indeterminate: Boolean get() = total == null
}

interface ProgressCell {
    val columnWidth: ColumnWidth

    fun ProgressState.makeRenderable(): Renderable
}

private fun ProgressCell.makeRenderable(state: ProgressState): Renderable = state.makeRenderable()

internal class TextProgressCell(private val text: Text) : ProgressCell {
    override val columnWidth: ColumnWidth get() = ColumnWidth.Auto
    override fun ProgressState.makeRenderable(): Renderable = text
}

internal class PercentageProgressCell(private val style: TextStyle) : ProgressCell {
    override val columnWidth: ColumnWidth get() = ColumnWidth.Fixed(4)
    override fun ProgressState.makeRenderable(): Renderable {
        return when (total) {
            null -> Text("", style)
            else -> Text("${(100 * completed / total)}%", style)
        }
    }
}

internal class CompletedProgressCell(
    private val suffix: String,
    private val includeTotal: Boolean,
    private val style: TextStyle
) : ProgressCell {
    override val columnWidth: ColumnWidth
        get() = ColumnWidth.Fixed((if (includeTotal) 11 else 5) + suffix.length)

    override fun ProgressState.makeRenderable(): Renderable {
        if (indeterminate) {
            return EmptyRenderable
        }

        val complete = completed.toDouble()
        val t = if (includeTotal && total != null) {
            val (nums, unit) = formatFloats(1, complete, total.toDouble())
            "${nums[0]}/${nums[1]}$unit"
        } else {
            complete.format(1)
        } + suffix
        return Text(t, style, whitespace = Whitespace.PRE)
    }
}

// TODO: this isn't thread safe
internal abstract class ThrottledProgressCell(frameRate: Int?) : ProgressCell {
    private var lastFrameTime = 0.0
    private val frameDuration = frameRate?.let { 1.0 / it }
    private fun shouldSkipUpdate(elapsed: Double): Boolean {
        if (frameDuration == null) return false
        if ((elapsed - lastFrameTime) < frameDuration) return true
        lastFrameTime = elapsed
        return false
    }

    private var renderable: Renderable? = null
    final override fun ProgressState.makeRenderable(): Renderable {
        var r = renderable
        val shouldSkipUpdate = shouldSkipUpdate(elapsedSeconds)
        if (r != null && shouldSkipUpdate) return r
        r = makeFreshRenderable()
        renderable = r
        return r
    }

    protected abstract fun ProgressState.makeFreshRenderable(): Renderable
}

internal class SpeedProgressCell(
    private val suffix: String,
    frameRate: Int?,
    private val style: TextStyle
) : ThrottledProgressCell(frameRate) {
    override val columnWidth: ColumnWidth get() = ColumnWidth.Fixed(5 + suffix.length)

    override fun ProgressState.makeFreshRenderable(): Renderable {
        return when {
            indeterminate || completedPerSecond <= 0 -> Text(" --.-$suffix", style)
            else -> Text(completedPerSecond.format(1) + suffix, style, whitespace = Whitespace.PRE)
        }
    }
}

internal class EtaProgressCell(
    private val prefix: String,
    frameRate: Int?,
    private val style: TextStyle,
) : ThrottledProgressCell(frameRate) {
    override val columnWidth: ColumnWidth get() = ColumnWidth.Fixed(7 + prefix.length)

    override fun ProgressState.makeFreshRenderable(): Renderable {
        val eta = if (total == null) 0.0 else (total - completed) / completedPerSecond
        if (indeterminate || eta < 0 || completedPerSecond == 0.0) {
            return text("$prefix-:--:--")
        }

        val h = (eta / (60 * 60)).toInt()
        val m = (eta / 60 % 60).toInt()
        val s = (eta % 60).roundToInt()

        return text("$prefix$h:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}")
    }

    private fun text(s: String) = Text(s, style, whitespace = Whitespace.PRE)
}

// TODO arguments to ProgressBar constructor
internal class BarProgressCell(val width: Int?) : ProgressCell {
    override val columnWidth: ColumnWidth
        get() = width?.let { ColumnWidth.Fixed(it) } ?: ColumnWidth.Expand()

    override fun ProgressState.makeRenderable(): Renderable {
        val frame = (elapsedSeconds * frameRate).toInt()
        return ProgressBar(total ?: 100, completed, indeterminate, width, pulseFrame = frame, pulseDuration = frameRate)
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
private class ProgressHistory(windowLengthSeconds: Float, private val timeSource: () -> Long) {
    private var startTime: Long = -1
    private val samples = ArrayDeque<ProgressHistoryEntry>()
    private val windowLengthNs = (TimeUnit.SECONDS.toNanos(1) * windowLengthSeconds).toLong()

    fun start() {
        if (startTime < 0) {
            startTime = timeSource()
        }
    }

    fun clear() {
        startTime = -1
        samples.clear()
    }

    fun update(completed: Int) {
        start()
        val now = timeSource()
        val keepTime = now - windowLengthNs
        while (samples.firstOrNull().let { it != null && it.timeNs < keepTime }) {
            samples.removeFirst()
        }
        samples.addLast(ProgressHistoryEntry(now, completed))
    }

    fun makeState(total: Int?, frameRate: Int) = ProgressState(
        completed = completed,
        total = total,
        completedPerSecond = completedPerSecond,
        elapsedSeconds = elapsedSeconds,
        frameRate = frameRate,
    )

    val completed: Int
        get() = samples.lastOrNull()?.completed ?: 0

    private val elapsedSeconds: Double
        get() = if (startTime >= 0) nanosToSeconds(timeSource() - startTime) else 0.0

    private val completedPerSecond: Double
        get() {
            if (startTime < 0 || samples.size < 2) return 0.0
            val sampleTimespan = nanosToSeconds(samples.last().timeNs - samples.first().timeNs)
            val complete = samples.last().completed - samples.first().completed
            return if (complete <= 0 || sampleTimespan <= 0) 0.0 else complete / sampleTimespan
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
    timeSource: () -> Long
) {
    private var total: Int? = null
    private val history = ProgressHistory(historyLength, timeSource)
    private val animation = t.animation<Unit> {
        grid {
            val state = history.makeState(total, frameRate)
            rowFrom(cells.map { it.makeRenderable(state) })
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

    fun update() {
        update(history.completed)
    }

    fun update(completed: Int) {
        history.update(completed)
        animation.update(Unit)
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

    fun advance(amount: Int = 1) {
        update(history.completed + amount)
    }

    fun start() {
        history.start()
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
        history.clear()
        animation.clear()
    }
}
