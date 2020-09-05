package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal
import com.github.ajalt.mordant.rendering.internal.parseText

class Text internal constructor(
        lines: Lines,
        private val style: TextStyle = TextStyle(),
        private val whitespace: Whitespace = Whitespace.NORMAL,
        private val align: TextAlign = TextAlign.LEFT
) : Renderable {
    private val lines = Lines(lines.lines.map { l -> l.map { it.withStyle(style) } })

    constructor(
            text: String,
            style: TextStyle = TextStyle(),
            whitespace: Whitespace = Whitespace.NORMAL,
            align: TextAlign = TextAlign.LEFT
    ) : this(parseText(text, style), style, whitespace, align)

    override fun measure(t: Terminal, width: Int): WidthRange {
        val lines = wrap(width)
        val min = lines.lines.maxOfOrNull { l -> l.maxOf { it.cellWidth } } ?: 0
        val max = lines.lines.maxOfOrNull { l -> l.sumOf { it.cellWidth } } ?: 0
        return WidthRange(min, max)
    }

    override fun render(t: Terminal, width: Int): Lines {
        // TODO: align
        return wrap(width)
    }

    /** Wrap spans to a list of lines. The last span in every line will be blank and end with `\n` */
    private fun wrap(wrapWidth: Int): Lines {
        // TODO: hard break on NEL
        val lines = mutableListOf<Line>()
        var line = mutableListOf<Span>()
        var width = 0
        var lastPieceWasWhitespace = true

        fun breakLine() {
            // TODO truncate whitespace
            if (whitespace.trimEol) {
                val lastNonWhitespace = line.indexOfLast { !it.isWhitespace() }
                repeat(line.lastIndex - lastNonWhitespace) { line.removeLast() }
            }

            lines += line
            line = mutableListOf()
            width = 0
            lastPieceWasWhitespace = true
        }

        for (oldLine in this.lines.lines) {
            // Add a space if this line was collapsed
            if (!lastPieceWasWhitespace) {
                line.add(Span.word(text = " ", style = line.lastOrNull()?.style ?: style))
                lastPieceWasWhitespace = true
            }

            for (piece in oldLine) {
                val pieceIsWhitespace = piece.isWhitespace()

                if (pieceIsWhitespace && lastPieceWasWhitespace && whitespace.collapseSpaces) continue

                // Collapse whitespace
                val span = when {
                    pieceIsWhitespace && whitespace.collapseSpaces -> piece.copy(text = " ")
                    else -> piece
                }

                // Don't add spaces to start of line
                if (whitespace.collapseSpaces && width == 0 && span.text.isBlank()) continue

                width += span.cellWidth
                line.add(span)

                // Break line if necessary
                // TODO break before word if width == wrapWidth
                if (whitespace.wrap && width >= wrapWidth) {
                    breakLine()
                } else {
                    lastPieceWasWhitespace = pieceIsWhitespace
                }
            }

            if (!whitespace.collapseNewlines) {
                breakLine()
            }
        }

        if (line.isNotEmpty()) lines += line

        return Lines(lines)
    }

    override fun toString(): String {
        val plain = lines.lines.flatten().joinToString("") { it.text }
        return "Text(${plain.take(25)}${if (plain.length > 25) "…" else ""})"
    }
}
