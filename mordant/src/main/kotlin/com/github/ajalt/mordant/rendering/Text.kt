package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal

class Text(
        private val spans: List<Span>,
        private val styles: Set<TextStyle> = emptySet(),
        private val whitespace: Whitespace = Whitespace.NORMAL,
        private val align: TextAlign = TextAlign.LEFT
) : Renderable {
    constructor(text: String) : this(listOf(Span(text)))

    override fun measure(t: Terminal): IntRange {
        val plain = plain()
        val max = plain.lineSequence().maxOfOrNull { it.length } ?: 0
        val min = plain.split(WHTIESPACE_REGEX).minOfOrNull { it.length } ?: 0
        return min..max
    }

    override fun render(t: Terminal): List<Span> {
        return wrap(t).flatten().map { it.withStyle(styles) }
    }

    /** Wrap spans to a list of lines. The last span in every line will be blank and end with `\n` */
    private fun wrap(t: Terminal, width: Int = t.width): MutableList<MutableList<Span>> {
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

            w += span.text.cellWidth()
            line.add(span)

            if (whitespace.wrap && w >= width) {
                lines += line
                line = mutableListOf()
                w = 0
            }
        }

        if (line.isNotEmpty()) lines += line

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

private val WHTIESPACE_REGEX = Regex("\\S+")

internal fun String.cellWidth() = length // TODO: calculate cell width
