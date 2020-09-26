package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal
import com.github.ajalt.mordant.rendering.TextAlign.*
import com.github.ajalt.mordant.rendering.internal.parseText

internal const val NEL = "\u0085"
internal const val LS = "\u2028"


class Text internal constructor(
        lines: Lines,
        private val style: TextStyle = DEFAULT_STYLE,
        private val whitespace: Whitespace = Whitespace.NORMAL,
        private val align: TextAlign = LEFT // TODO wordwrap (truncate, ellipses, wrap)
) : Renderable {
    private val lines = Lines(lines.lines.map { l -> l.map { it.withStyle(style) } })

    constructor(
            text: String,
            style: TextStyle = DEFAULT_STYLE,
            whitespace: Whitespace = Whitespace.NORMAL,
            align: TextAlign = LEFT,
    ) : this(parseText(text, style), style, whitespace, align)

    override fun measure(t: Terminal, width: Int): WidthRange {
        val lines = wrap(width)
        val min = lines.lines.maxOfOrNull { l -> l.maxOfOrNull { it.cellWidth } ?: 0 } ?: 0
        val max = lines.lines.maxOfOrNull { l -> l.sumOf { it.cellWidth } } ?: 0
        return WidthRange(min, max)
    }

    override fun render(t: Terminal, width: Int): Lines {
        return wrap(width)
    }

    private fun wrap(wrapWidth: Int): Lines {
        val lines = mutableListOf<Line>()
        var line = mutableListOf<Span>()
        var width = 0
        var lastPieceWasWhitespace = true


        fun breakLine() {
            // TODO truncate whitespace
            if (whitespace.trimEol || align == JUSTIFY) {
                val lastNonWhitespace = line.indexOfLast { !it.isWhitespace() }
                if (lastNonWhitespace >= 0) {
                    repeat(line.lastIndex - lastNonWhitespace) { line.removeLast() }
                }
            }

            if (width < wrapWidth) {
                val extraWidth = wrapWidth - width
                when (align) {
                    LEFT -> alignLineLeft(line, extraWidth)
                    RIGHT -> alignLineRight(line, extraWidth)
                    CENTER -> alignLineCenter(line, extraWidth)
                    JUSTIFY -> line = justifyLine(line, extraWidth)
                    NONE -> {
                    }
                }
            }


            lines += line
            line = mutableListOf()
            width = 0
            lastPieceWasWhitespace = true
        }

        for (oldLine in this.lines.lines) {
            // Add a space if this line was collapsed
            if (!lastPieceWasWhitespace) {
                val style = when (line.last().style) {
                    oldLine.firstOrNull()?.style -> line.last().style
                    else -> style
                }
                line.add(Span.word(text = " ", style = style))
                lastPieceWasWhitespace = true
                width += 1
            }

            for (piece in oldLine) {
                if (piece.text == NEL || piece.text == LS) {
                    breakLine()
                    continue
                }
                val pieceIsWhitespace = piece.isWhitespace()

                // Collapse whitespace
                if (pieceIsWhitespace && lastPieceWasWhitespace && whitespace.collapseSpaces) continue
                val span = when {
                    pieceIsWhitespace && whitespace.collapseSpaces -> piece.copy(text = " ")
                    else -> piece
                }

                // Break line if necessary
                if (whitespace.wrap && width > 0 && width + span.cellWidth > wrapWidth) {
                    breakLine()
                }

                // Don't add spaces to start of line
                if (whitespace.collapseSpaces && width == 0 && span.text.isBlank()) continue

                width += span.cellWidth
                line.add(span)

                lastPieceWasWhitespace = pieceIsWhitespace
            }

            if (!whitespace.collapseNewlines) {
                breakLine()
            }
        }

        if (line.isNotEmpty()) lines += line

        return Lines(lines)
    }

    private fun alignLineLeft(line: MutableList<Span>, extraWidth: Int) {
        line.add(Span.space(extraWidth, line.lastOrNull()?.style ?: DEFAULT_STYLE))
    }

    private fun alignLineRight(line: MutableList<Span>, extraWidth: Int) {
        line.add(0, Span.space(extraWidth, line.firstOrNull()?.style ?: DEFAULT_STYLE))
    }

    private fun alignLineCenter(line: MutableList<Span>, extraWidth: Int) {
        val halfExtra = extraWidth / 2
        alignLineLeft(line, halfExtra + extraWidth % 2)
        if (halfExtra > 0) alignLineRight(line, halfExtra)
    }

    private fun justifyLine(line: MutableList<Span>, extraWidth: Int): MutableList<Span> {
        val spaceCount = line.count { it.isWhitespace() }

        if (spaceCount == 0) {
            alignLineCenter(line, extraWidth)
            return line
        }

        val spaceSize = extraWidth / spaceCount
        var skipRemainder = spaceCount - extraWidth % spaceCount
        val justifiedLine = ArrayList<Span>(line.size + skipRemainder + if (spaceSize > 0) spaceCount else 0)
        for (span in line) {
            justifiedLine += span
            if (!span.isWhitespace()) continue
            if (skipRemainder-- > 0 && spaceSize == 0) continue
            justifiedLine += Span.space(spaceSize + if (skipRemainder < 0) 1 else 0, span.style)
        }
        return justifiedLine
    }

    override fun toString(): String {
        val plain = lines.lines.flatten().joinToString("") { it.text }
        return "Text(${plain.take(25)}${if (plain.length > 25) "…" else ""})"
    }
}
