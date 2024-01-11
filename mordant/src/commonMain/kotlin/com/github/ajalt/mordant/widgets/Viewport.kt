package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.Lines
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.rendering.WidthRange
import com.github.ajalt.mordant.rendering.setSize
import com.github.ajalt.mordant.terminal.Terminal


/**
 * Crop or pad another [content] widget to a fixed size, and optionally scroll visible portion of
 * the widget within that size.
 *
 * If [width] or [height] are larger than the size of the [content] widget, the extra space will be
 * filled with spaces.
 *
 * You can specify [scrollRight] and [scrollDown] to scroll the viewport across the content.
 * Negative values will scroll the widget to the right or up, respectively.
 *
 * ### Example
 *
 * ```
 * val text = Text(
 *     """
 *     123
 *     456
 *     7890
 *     """.trimIndent()
 * )
 *
 * val viewport1 = Viewport(text, width = 2, height = 2, scrollRight = 1, scrollDown = 1)
 * terminal.println(Panel(viewport1))
 *
 * val viewport2 = Viewport(text, width = 2, height = 2, scrollRight = -1, scrollDown = -1)
 * terminal.println(Panel(viewport2))
 * ```
 *
 * Will print the following:
 *
 * ```
 * ╭──╮
 * │56│
 * │89│
 * ╰──╯
 * ╭──╮
 * │  │
 * │ 1│
 * ╰──╯
 * ```
 */
class Viewport(
    /** The widget to crop. */
    private val content: Widget,
    /** The width to crop the widget to, or `null` to use the width of the longest line */
    private val width: Int?,
    /** The height to crop the widget to, or `null` to leave the height unchanged */
    private val height: Int? = null,
    /**
     * The number of characters to crop from the left of the [content] (or from the right, if
     * negative)
     */
    private val scrollRight: Int = 0,
    /**
     * The number of lines to crop from the top of the [content] (or from the bottom, if negative)
     */
    private val scrollDown: Int = 0,
) : Widget {
    override fun measure(t: Terminal, width: Int): WidthRange {
        return if (this.width != null) {
            WidthRange(this.width, this.width)
        } else {
            content.measure(t, width)
        }
    }

    override fun render(t: Terminal, width: Int): Lines {
        val lines = content.render(t, width)
        return lines.setSize(
            newWidth = this.width ?: lines.width,
            newHeight = this.height ?: lines.lines.size,
            scrollRight = scrollRight,
            scrollDown = scrollDown
        )
    }
}
