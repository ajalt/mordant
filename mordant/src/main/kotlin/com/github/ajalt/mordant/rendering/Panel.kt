package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal

// TODO: test
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

        lines.add(listOf(borderStyle.renderTop(listOf(contentWidth))))

        val left = listOf(Span.word(borderStyle.body.left, borderTextStyle))
        val right = listOf(Span.word(borderStyle.body.right, borderTextStyle))

        val aligned = renderedContent.setSize(contentWidth, align = TextAlign.LEFT)
        aligned.lines.mapTo(lines) { line ->
            listOf(left, line, right).flatten()
        }

        lines.add(listOf(borderStyle.renderBottom(listOf(contentWidth))))
        return Lines(lines)
    }
}
