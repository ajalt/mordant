package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal

class Text(
        private val spans: List<Span>,
        private val style: TextStyle = TextStyle(),
        private val whitespace: Whitespace = Whitespace.NORMAL,
        private val align: TextAlign = TextAlign.LEFT
) : Renderable {
    constructor(text: String) : this(listOf(Span(text)))

    override fun measure(t: Terminal, width: Int): IntRange {
        val lines = wrap(width)
        val max = lines.maxOfOrNull { l -> l.sumOf { it.cellWidth } } ?: 0
        val min = lines.maxOfOrNull { l -> l.maxOf { it.cellWidth } } ?: 0
        return min..max
    }

    override fun render(t: Terminal, width: Int): List<Span> {
        return wrap(width).flatten().map { it.withStyle(style) }
    }

    /** Wrap spans to a list of lines. The last span in every line will be blank and end with `\n` */
    private fun wrap(width: Int): MutableList<MutableList<Span>> {
        val pieces = spans.asSequence().flatMap { it.split(whitespace.collapseNewlines) }
        val lines = mutableListOf<MutableList<Span>>()
        var line = mutableListOf<Span>()
        var w = 0

        for (piece in pieces) {
            assert(piece.text.isNotEmpty())

            val span = when {
                piece.text.isBlank() && whitespace.collapseSpaces -> piece.copy(text = " ")
                else -> piece
            }

            // Don't add spaces to start of line
            if (w == 0 && span.text != "\n" && span.text.isBlank()) continue

            w += span.cellWidth
            line.add(span)

            if (whitespace.wrap && w >= width) {
                lines += line
                line = mutableListOf()
                w = 0
            }
        }

        if (line.isNotEmpty()) lines += line

        // Trim trailing whitespace if necessary, and ensure all lines end with a line break
        for (l in lines) {
            val last = l.last()
            if (!last.text.endsWith("\n")) l.add(last.copy(text = "\n"))
            else if (whitespace.trimEol) {
                l[l.lastIndex] = last.copy(text = "\n")
            }
        }

        return lines
    }

    private fun plain(): String = spans.joinToString("") { it.text }

    override fun toString(): String {
        val plain = plain()
        return "Text(${plain.take(25)}${if (plain.length > 25) "…" else ""})"
    }
}
