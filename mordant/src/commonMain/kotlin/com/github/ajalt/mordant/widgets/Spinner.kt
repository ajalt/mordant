package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.MppAtomicInt
import com.github.ajalt.mordant.rendering.Lines
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.rendering.WidthRange
import com.github.ajalt.mordant.terminal.Terminal

/**
 * A widget that will loop through a fixed list of frames.
 *
 * The widget will render the same frame until [tick] is set or [advanceTick] is called, at which point the next frame
 * will render. The animation will loop when [tick] is larger than the number frames.
 *
 * To reduce the speed of the animation, increase the [duration].
 *
 * @property duration the number of [ticks][tick] that each frame of the spinner should show for. This defaults to 1,
 *   which will cause a new frame to display every time [advanceTick] is called.
 * @param initial the starting tick value.
 */
class Spinner(
    private val frames: List<Widget>,
    private val duration: Int = 1,
    initial: Int = 0,
) : Widget {
    companion object {
        /**
         * Create a spinner with the following frames:
         *
         * `⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏`
         */
        fun Dots(style: TextStyle = DEFAULT_STYLE, duration: Int = 1, initial: Int = 0): Spinner {
            return Spinner("⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏", style, duration, initial)
        }

        /**
         * Create a spinner with the following frames:
         *
         * `|/-\`
         *
         * These frames only use ASCII characters.
         */
        fun Lines(style: TextStyle = DEFAULT_STYLE, duration: Int = 1, initial: Int = 0): Spinner {
            return Spinner("|/-\\", style, duration, initial)
        }
    }

    /**
     * Construct a [Spinner] from a string, where each character in the string is one frame.
     */
    constructor(frames: String, style: TextStyle = DEFAULT_STYLE, duration: Int = 1, initial: Int = 0) :
            this(frames.map { Text(style(it.toString())) }, duration, initial)

    private val _tick = MppAtomicInt(initial)

    /**
     * The current frame number.
     *
     * This may be larger than the number of frames, in which case the animation will loop.
     */
    var tick: Int
        get() = _tick.get()
        set(value) {
            _tick.set(value)
        }

    /**
     * Increment the [tick] value by one and return the new value.
     */
    fun advanceTick(): Int {
        return _tick.getAndIncrement() + 1
    }

    /** The current frame */
    val currentFrame: Widget get() = frames[(tick / duration) % frames.size]

    override fun measure(t: Terminal, width: Int): WidthRange = currentFrame.measure(t, width)
    override fun render(t: Terminal, width: Int): Lines = currentFrame.render(t, width)
}
