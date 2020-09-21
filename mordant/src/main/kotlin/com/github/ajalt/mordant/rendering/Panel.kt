package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal

class Panel(
        content: Renderable,
        private val borderStyle: BorderStyle = BorderStyle.SQUARE,
        private val expand: Boolean = false,
        private val borderTextStyle: TextStyle = DEFAULT_STYLE,
        padding: Padding = DEFAULT_PADDING
) : Renderable {
    private val content: Renderable = Padded.get(content, padding)

    override fun measure(t: Terminal, width: Int): WidthRange {
        val measurement = content.measure(t, width - 2)

        return if (expand) {
            WidthRange(measurement.max + 2, measurement.max + 2)
        } else {
            measurement + 2
        }
    }

    override fun render(t: Terminal, width: Int): Lines {
        val maxContentWidth = width - 2
        val measurement = content.measure(t, maxContentWidth)
        val renderedContent = content.render(t, maxContentWidth)
        val lines = ArrayList<Line>(renderedContent.lines.size + 2)

        val contentWidth = when {
            expand -> maxContentWidth
            else -> measurement.max.coerceAtMost(maxContentWidth)
        }

        val b = borderStyle.body
        val horizontalBorder = Span.word(b.ew.repeat(contentWidth))
        lines.add(listOf(Span.word(b.es, borderTextStyle), horizontalBorder, Span.word(b.sw, borderTextStyle)))

        val vertical = listOf(Span.word(b.ns, borderTextStyle))

        val aligned = renderedContent.setSize(contentWidth, align = TextAlign.CENTER)
        aligned.lines.mapTo(lines) { line ->
            listOf(vertical, line, vertical).flatten()
        }

        lines.add(listOf(Span.word(b.ne, borderTextStyle), horizontalBorder, Span.word(b.nw, borderTextStyle)))
        return Lines(lines)
    }
}
