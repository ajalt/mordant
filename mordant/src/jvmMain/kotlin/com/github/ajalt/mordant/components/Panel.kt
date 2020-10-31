package com.github.ajalt.mordant.components

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextAlign.LEFT

class Panel(
        content: Renderable,
        private val borderStyle: BorderStyle? = BorderStyle.ROUNDED,
        private val expand: Boolean = false,
        private val borderTextStyle: TextStyle = DEFAULT_STYLE,
        padding: Padding = DEFAULT_PADDING
) : Renderable {
    constructor(
            content: String,
            borderStyle: BorderStyle? = BorderStyle.ROUNDED,
            expand: Boolean = false,
            borderTextStyle: TextStyle = DEFAULT_STYLE,
            padding: Padding = DEFAULT_PADDING
    ) : this(Text(content), borderStyle, expand, borderTextStyle, padding)

    private val content: Renderable = Padded.get(content, padding)
    private val borderWidth get() = if (borderStyle == null) 0 else 2

    override fun measure(t: Terminal, width: Int): WidthRange {
        val measurement = content.measure(t, width - borderWidth)

        return if (expand) {
            WidthRange(measurement.max + borderWidth, measurement.max + borderWidth)
        } else {
            measurement + borderWidth
        }
    }

    override fun render(t: Terminal, width: Int): Lines {
        val maxContentWidth = width - borderWidth
        val measurement = content.measure(t, maxContentWidth)

        val contentWidth = when {
            expand -> maxContentWidth
            else -> measurement.max.coerceAtMost(maxContentWidth)
        }

        val renderedContent = content.render(t, maxContentWidth).setSize(contentWidth, textAlign = LEFT)
        if (borderStyle == null) return renderedContent

        val lines = ArrayList<Line>(renderedContent.lines.size + borderWidth)
        val b = borderStyle.body
        val horizontalBorder = Span.word(b.ew.repeat(contentWidth), borderTextStyle)
        lines.add(listOf(Span.word(b.es, borderTextStyle), horizontalBorder, Span.word(b.sw, borderTextStyle)))

        val vertical = listOf(Span.word(b.ns, borderTextStyle))

        renderedContent.lines.mapTo(lines) { line ->
            flatLine(vertical, line, vertical)
        }

        lines.add(listOf(Span.word(b.ne, borderTextStyle), horizontalBorder, Span.word(b.nw, borderTextStyle)))
        return Lines(lines)
    }
}
