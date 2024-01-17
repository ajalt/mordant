package com.github.ajalt.mordant.widgets.progress

import com.github.ajalt.mordant.internal.MppAtomicRef
import com.github.ajalt.mordant.internal.update
import com.github.ajalt.mordant.rendering.Widget
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

// TODO: docs, review all of these interface names
/**
 * A [ProgressBarWidgetMaker] that caches the widgets for each cell so that they only update as fast
 * as their [fps][ProgressBarCell.fps].
 */
interface CachedProgressBarWidgetMaker<T> {
    /**
     * Build a progress bar widget from the given [states], using the cached definition.
     */
    fun build(states: List<ProgressState<T>>): Widget

    /**
     * Invalidate the cache for the task with id [taskId], or all tasks if [taskId] is null.
     */
    fun invalidateCache(taskId: TaskId? = null)

    /**
     * The refresh rate, in frames per second, that will satisfy the [fps][ProgressBarCell.fps] of
     * all this progress bar's cells.
     */
    val refreshRate: Int

    /** The TimeSource used by this cache */
    val timeSource: TimeSource.WithComparableMarks
}

/** The time between refreshes. This is `1 / refreshRate` */
val CachedProgressBarWidgetMaker<*>.refreshPeriod: Duration
    get() = (1.0 / refreshRate).seconds

// TODO: docs
fun <T> CachedProgressBarWidgetMaker<T>.build(vararg states: ProgressState<T>): Widget {
    return build(states.asList())
}

fun <T> CachedProgressBarWidgetMaker<T>.build(
    context: T,
    total: Long?,
    completed: Long,
    displayedTime: ComparableTimeMark,
    status: ProgressState.Status = ProgressState.Status.NotStarted,
    speed: Double? = null,
): Widget {
    val state = ProgressState(
        context, total, completed, displayedTime, status, speed
    )
    return build(state)
}

fun CachedProgressBarWidgetMaker<Unit>.build(
    total: Long?,
    completed: Long,
    displayedTime: ComparableTimeMark,
    status: ProgressState.Status = ProgressState.Status.NotStarted,
    speed: Double? = null,
): Widget {
    return build(Unit, total, completed, displayedTime, status, speed)
}

fun <T> ProgressBarDefinition<T>.cache(
    timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    maker: ProgressBarWidgetMaker = BaseProgressBarWidgetMaker,
): CachedProgressBarWidgetMaker<T> {
    return CachedProgressBarWidgetMakerImpl(this, maker, timeSource)
}

private class CachedProgressBarWidgetMakerImpl<T>(
    definition: ProgressBarDefinition<T>,
    private val maker: ProgressBarWidgetMaker,
    override val timeSource: TimeSource.WithComparableMarks,
) : CachedProgressBarWidgetMaker<T> {
    private data class Invalidations(
        val all: ComparableTimeMark,
        val byTask: Map<TaskId, ComparableTimeMark>,
    )

    private val d = ProgressBarDefinition(
        definition.cells.map { makeCell(it) },
        definition.spacing,
        definition.alignColumns,
    )

    private val invalidations = MppAtomicRef(
        // Start with an invalidation time in the past so that the first frame is always rendered
        Invalidations(timeSource.markNow() - 99.days, emptyMap())
    )

    private fun makeCell(
        cell: ProgressBarCell<T>,
    ): ProgressBarCell<T> {
        // Wrap the cell builder in a block that caches the widget
        val cachedWidgets = MppAtomicRef(mapOf<TaskId, Pair<ComparableTimeMark, Widget>>())
        return ProgressBarCell(cell.columnWidth, cell.fps, cell.align, cell.verticalAlign) {
            val (_, new) = cachedWidgets.update {
                when {
                    isCacheValid(cell, this) -> this
                    else -> {
                        val content = cell.content(this@ProgressBarCell)
                        this + (taskId to (timeSource.markNow() to content))
                    }
                }
            }

            new[taskId]?.second ?: cell.content(this)
        }
    }

    private fun ProgressState<T>.isCacheValid(
        cell: ProgressBarCell<T>,
        cachedWidgets: Map<TaskId, Pair<ComparableTimeMark, Widget>>,
    ): Boolean {
        val (lastFrameTime, _) = cachedWidgets[taskId] ?: return false
        val invals = invalidations.value
        val invalAt = maxOf(invals.all, invals.byTask[taskId] ?: invals.all)
        if (lastFrameTime <= invalAt) return false
        val timeSinceLastFrame = lastFrameTime.elapsedNow()
        // if fps is 0 this will be Infinity, so it will be cached forever
        val maxCacheRetentionDuration = (1.0 / cell.fps).seconds
        return timeSinceLastFrame < maxCacheRetentionDuration
    }

    override fun invalidateCache(taskId: TaskId?) {
        invalidations.update {
            copy(
                all = if (taskId == null) timeSource.markNow() else all,
                byTask = if (taskId != null) byTask + (taskId to timeSource.markNow()) else byTask,
            )
        }
    }

    override fun build(states: List<ProgressState<T>>): Widget {
        return maker.build(d, states)
    }

    override val refreshRate: Int get() = d.cells.maxOfOrNull { it.fps } ?: 0
}
