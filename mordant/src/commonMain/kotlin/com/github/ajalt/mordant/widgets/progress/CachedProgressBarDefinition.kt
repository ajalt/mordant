package com.github.ajalt.mordant.widgets.progress

import com.github.ajalt.mordant.internal.MppAtomicRef
import com.github.ajalt.mordant.internal.update
import com.github.ajalt.mordant.rendering.Widget
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource


/**
 * A [ProgressBarDefinition] that caches the widgets for each cell so that they only update as fast
 * as their [fps][ProgressBarCell.fps].
 */
class CachedProgressBarDefinition<T>(
    definition: ProgressBarDefinition<T>,
    private val timeSource: TimeSource.WithComparableMarks,
) : ProgressBarDefinition<T> {
    private val cache = MppAtomicRef<Map<Int, Pair<ComparableTimeMark, Widget>>>(emptyMap())
    override val cells: List<ProgressBarCell<T>> =
        definition.cells.mapIndexed { i, it -> makeCell(i, it) }
    override val spacing: Int = definition.spacing
    override val alignColumns: Boolean = definition.alignColumns

    /**
     * Invalidate the cache for this definition.
     */
    fun invalidateCache() {
        cache.getAndSet(emptyMap())
    }

    /**
     * The refresh rate, Hz, that will satisfy the [fps][ProgressBarCell.fps] of
     * all this progress bar's cells.
     */
    val fps: Int = definition.cells.maxOfOrNull { it.fps } ?: 0

    // Wrap the cell builder in a block that caches the widget
    private fun makeCell(
        i: Int,
        cell: ProgressBarCell<T>,
    ): ProgressBarCell<T> {
        return ProgressBarCell(cell.columnWidth, cell.fps, cell.align, cell.verticalAlign) {
            val (_, new) = cache.update {
                when {
                    isCacheValid(cell, this[i]?.first) -> this
                    else -> {
                        val content = cell.content(this@ProgressBarCell)
                        this + (i to (timeSource.markNow() to content))
                    }
                }
            }

            new[i]?.second ?: cell.content(this)
        }
    }

    private fun isCacheValid(
        cell: ProgressBarCell<T>, lastFrameTime: ComparableTimeMark?,
    ): Boolean {
        if (lastFrameTime == null) return false
        val timeSinceLastFrame = lastFrameTime.elapsedNow()
        // if fps is 0 this will be Infinity, so it will be cached forever
        val maxCacheRetentionDuration = (1.0 / cell.fps).seconds
        return timeSinceLastFrame < maxCacheRetentionDuration
    }
}

/**
 * Cache this progress bar definition so that each cell only updates as often as its
 * [fps][ProgressBarCell.fps].
 */
fun <T> ProgressBarDefinition<T>.cache(
    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
): CachedProgressBarDefinition<T> {
    return when (this) {
        is CachedProgressBarDefinition -> this
        else -> CachedProgressBarDefinition(this, timeSource)
    }
}
