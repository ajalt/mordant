package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.Lines
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.rendering.WidthRange
import com.github.ajalt.mordant.rendering.setSize
import com.github.ajalt.mordant.terminal.Terminal


/**
 * Crop or pad another [content] widget to a fixed size.
 *
 * If [width] or [height] are larger than the size of the [content] widget, the extra space will be
 * filled with spaces.
 */
class Crop(
    /** The widget to crop. */
    private val content: Widget,
    /** The width to crop the widget to, or `null` to leave the width unchanged */
    private val width: Int?,
    /** The height to crop the widget to, or `null` to leave the height unchanged */
    private val height: Int? = null,
): Widget{
    override fun measure(t: Terminal, width: Int): WidthRange {
        return if (this.width != null) {
            WidthRange(this.width, this.width)
        } else {
            content.measure(t, width)
        }
    }

    override fun render(t: Terminal, width: Int): Lines {
        val lines = content.render(t, width)
        return lines.setSize(this.width, this.height ?: lines.lines.size)
    }
}
