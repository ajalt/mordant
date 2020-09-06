package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal

class Panel(
        content: Renderable,
        private val borders: Borders = Borders.SQUARE,
        private val expand: Boolean = false,
        private val borderStyle: TextStyle = DEFAULT_STYLE,
        padding: Padding = Padding()
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

        lines.add(listOf(borders.renderTop(listOf(contentWidth))))

        val left = listOf(Span.word(borders.body.left, borderStyle))
        val right = listOf(Span.word(borders.body.right, borderStyle))

        val width1 = renderedContent.setWidth(contentWidth)
        width1.lines.mapTo(lines) { line ->
            listOf(left, line, right).flatten()
        }

        lines.add(listOf(borders.renderBottom(listOf(contentWidth))))
        return Lines(lines)
    }
}
