package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.Widget
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

open class ProgressBuilder internal constructor(
    private val builder: ProgressBarFactoryBuilder<Unit>,
) {
    var padding: Int = 2

    /**
     * Add a fixed text cell to this layout.
     */
    fun text(text: String) {
        builder.text(text)
    }

    /**
     * Add a percentage cell to this layout.
     */
    fun percentage() {
        builder.percentage()
    }

    /**
     * Add a progress bar cell to this layout.
     *
     * @param width The width in characters for this widget
     * @param pendingChar (theme string: "progressbar.pending") The character to use to draw the pending portion of the bar in the active state.
     * @param separatorChar (theme string: "progressbar.separator") The character to draw in between the competed and pending bar in the active state.
     * @param completeChar (theme string: "progressbar.complete") The character to use to draw the completed portion of the bar in the active state.
     * @param pendingStyle(theme style: "progressbar.pending") The style to use for the [pendingChar]s
     * @param separatorStyle (theme style: "progressbar.separator") The style to use for the [separatorChar]
     * @param completeStyle (theme style: "progressbar.complete") The style to use for the [completeChar] when completed < total
     * @param finishedStyle (theme style: "progressbar.complete") The style to use for the [completeChar] when total <= completed
     * @param indeterminateStyle e (theme style: "progressbar.separator") The style to use when the state us indeterminate
     * @param showPulse (theme flag: "progressbar.pulse") If false, never draw the pulse animation in the indeterminate state.
     */
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
        showPulse: Boolean? = null,
    ) {
        builder.progressBar(
            width,
            pendingChar,
            separatorChar,
            completeChar,
            pendingStyle,
            separatorStyle,
            completeStyle,
            finishedStyle,
            indeterminateStyle,
            showPulse,
        )
    }

    /**
     * Add a cell that displays the current completed count to this layout.
     */
    fun completed(
        suffix: String = "",
        includeTotal: Boolean = true,
        style: TextStyle = DEFAULT_STYLE,
    ) {
        builder.completed(suffix, includeTotal, style)
    }

    /**
     * Add a cell that displays the current speed to this layout.
     */
    fun speed(suffix: String = "it/s", style: TextStyle = DEFAULT_STYLE) {
        builder.speed(suffix, style)
    }

    /**
     * Add a cell that displays the time remaining to this layout.
     */
    fun timeRemaining(prefix: String = "eta ", style: TextStyle = DEFAULT_STYLE) {
        builder.timeRemaining(prefix, false, style)
    }

    /**
     * Add a [Spinner] to this layout.
     *
     * @param spinner The spinner to display
     * @param frameRate The number of times per second to advance the spinner's displayed frame
     */
    fun spinner(spinner: Spinner, frameRate: Int = 8) {
        builder.spinner(spinner, frameRate)
    }

    internal fun build(): ProgressLayout {
        return ProgressLayout(builder.build(padding, true))
    }
}

/**
 * A builder for creating an animated progress bar widget.
 */
class ProgressLayout internal constructor(
    private val factory: ProgressBarWidgetFactory<Unit>,
) {
    fun build(
        completed: Long,
        total: Long? = null,
        elapsedSeconds: Double = 0.0,
        completedPerSecond: Double? = null,
    ): Widget {
        val t = TestTimeSource()
        val displayedTime = t.markNow()
        t += elapsedSeconds.seconds
        val speed = (completedPerSecond ?: calcHz(completed, elapsedSeconds.seconds))
            .takeIf { it > 0 }
        return factory.build(
            ProgressState(
                total,
                completed,
                displayedTime,
                displayedTime,
                speed = speed
            )
        )
    }
}

/**
 * Build a [ProgressLayout]
 */
fun progressLayout(init: ProgressBuilder.() -> Unit): ProgressLayout {
    return ProgressBuilder(ProgressBarWidgetBuilder()).apply(init).build()
}
