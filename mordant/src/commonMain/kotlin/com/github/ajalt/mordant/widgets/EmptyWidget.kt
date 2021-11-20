package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.EMPTY_LINES
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.rendering.WidthRange
import com.github.ajalt.mordant.terminal.Terminal

/**
 * A widget with 0 width, 0 height, and no content.
 *
 * Can be used as a placeholder in layouts etc.
 */
object EmptyWidget : Widget {
    override fun measure(t: Terminal, width: Int) = WidthRange(0, 0)
    override fun render(t: Terminal, width: Int) = EMPTY_LINES
}
