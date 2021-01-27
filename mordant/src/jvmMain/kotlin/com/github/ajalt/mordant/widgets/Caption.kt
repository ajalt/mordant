package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal

/** Add a top and/or bottom caption to [content] */
class Caption(
    val content: Widget,
    val top: Widget? = null,
    val bottom: Widget? = null,
) : Widget {
    constructor(
        content: Widget,
        top: String? = null,
        bottom: String? = null,
        topAlign: TextAlign = TextAlign.CENTER,
        bottomAlign: TextAlign = TextAlign.CENTER,
        topStyle: TextStyle = DEFAULT_STYLE,
        bottomStyle: TextStyle = DEFAULT_STYLE,
    ) : this(
        content,
        top?.let { Text(it, topStyle, align = topAlign) },
        bottom?.let { Text(it, bottomStyle, align = bottomAlign) },
    )

    override fun measure(t: Terminal, width: Int): WidthRange {
        return content.measure(t, width)
    }

    override fun render(t: Terminal, width: Int): Lines {
        val captionWidth = content.measure(t, width).max
        val lines = mutableListOf<Line>()
        top?.let { lines.addAll(it.render(t, captionWidth).lines) }
        lines.addAll(content.render(t, width).lines)
        bottom?.let { lines.addAll(it.render(t, captionWidth).lines) }
        return Lines(lines)
    }
}
