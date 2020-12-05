package com.github.ajalt.mordant.components

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextAlign.*
import com.github.ajalt.mordant.internal.parseText

internal const val NEL = "\u0085"
internal const val LS = "\u2028"


class Text internal constructor(
        lines: Lines,
        private val style: TextStyle = DEFAULT_STYLE,
        private val whitespace: Whitespace = Whitespace.NORMAL,
        private val align: TextAlign = NONE,
        private val overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        private val width: Int? = null,
        private val tabWidth: Int? = null
) : Renderable {
    constructor(
            text: String,
            style: TextStyle = DEFAULT_STYLE,
            whitespace: Whitespace = Whitespace.NORMAL,
            align: TextAlign = NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null,
            tabWidth: Int? = null
    ) : this(parseText(text, style), style, whitespace, align, overflowWrap, width, tabWidth)

    init {
        require(width == null || width >= 0) { "width cannot be negative" }
        require(tabWidth == null || tabWidth >= 0) { "tab width cannot be negative" }
    }

    private val lines = when {
        style === DEFAULT_STYLE -> lines
        else -> Lines(lines.lines.map { l -> l.map { it.withStyle(style) } })
    }

    internal fun withAlign(align: TextAlign, overflowWrap: OverflowWrap?): Text {
        return when {
            align == this.align && (overflowWrap == null || overflowWrap == this.overflowWrap) -> this
            else -> Text(lines, style, whitespace, align, overflowWrap ?: this.overflowWrap)
        }
    }

    override fun measure(t: Terminal, width: Int): WidthRange {
        // measure without word wrap or padding from alignment
        val lines = wrap(this.width ?: width, tabWidth ?: t.tabWidth, NONE, OverflowWrap.NORMAL)
        val min = lines.lines.maxOfOrNull { l -> l.maxOfOrNull { it.cellWidth } ?: 0 } ?: 0
        val max = lines.lines.maxOfOrNull { l -> l.sumOf { it.cellWidth } } ?: 0
        return WidthRange(min, max)
    }

    override fun render(t: Terminal, width: Int): Lines {
        return wrap(this.width ?: width, tabWidth ?: t.tabWidth, align, overflowWrap)
    }

    private fun wrap(wrapWidth: Int, tabWidth: Int, align: TextAlign, overflowWrap: OverflowWrap): Lines {
        if (wrapWidth == 0 && overflowWrap != OverflowWrap.NORMAL) return EMPTY_LINES

        val lines = mutableListOf<Line>()
        var line = mutableListOf<Span>()
        var width = 0
        var lastPieceWasWhitespace = true

        fun breakLine() {
            if (whitespace.trimEol || align == JUSTIFY) {
                val lastNonWhitespace = lastNonWhitespace(line, align)
                if (lastNonWhitespace >= 0) {
                    repeat(line.lastIndex - lastNonWhitespace) { line.removeAt(line.lastIndex) }
                }
            }

            if (width < wrapWidth) {
                line = alignLine(line, wrapWidth, width, align)
            }

            lines += line
            line = mutableListOf()
            width = 0
            lastPieceWasWhitespace = true
        }

        for (oldLine in this.lines.lines) {
            val lastNonWhitespace = lastNonWhitespace(oldLine, align)

            loop@ for ((i, piece) in oldLine.withIndex()) {
                // Treat NEL and LS as hard line breaks
                if (piece.text == NEL || piece.text == LS) {
                    breakLine()
                    continue
                }

                // Trim trailing whitespace pieces. Continue rather than break in case there is a trailing NEL
                if ((whitespace.trimEol || align == JUSTIFY) && lastNonWhitespace in 0 until i) {
                    continue
                }

                // Add a space if this line was collapsed
                if (i == 0 && !lastPieceWasWhitespace) {
                    val style = when (line.last().style) {
                        oldLine.firstOrNull()?.style -> line.last().style
                        else -> style
                    }
                    line.add(Span.word(text = " ", style = style))
                    lastPieceWasWhitespace = true
                    width += 1
                }

                val pieceIsWhitespace = piece.isWhitespace()

                // Collapse spaces
                if (pieceIsWhitespace && lastPieceWasWhitespace && whitespace.collapseSpaces) continue
                var span = when {
                    pieceIsWhitespace && whitespace.collapseSpaces -> Span.space(1, piece.style)
                    piece.isTab() -> if (tabWidth > 0) Span.space(tabWidth - (width % tabWidth), piece.style) else continue
                    else -> piece
                }

                // Wrap line if necessary
                val cellWidth = span.cellWidth
                if (whitespace.wrap && width > 0 && width + cellWidth > wrapWidth) {
                    breakLine()
                    // Don't add spaces to start of line
                    if (pieceIsWhitespace) continue
                }

                // overflow wrap
                if (span.cellWidth > wrapWidth) {
                    when (overflowWrap) {
                        OverflowWrap.NORMAL -> {
                        }
                        OverflowWrap.TRUNCATE -> {
                            span = Span.word(span.text.take(wrapWidth), span.style)
                        }
                        OverflowWrap.ELLIPSES -> {
                            span = Span.word(span.text.take((wrapWidth - 1)) + "…", span.style)
                        }
                        OverflowWrap.BREAK_WORD -> {
                            span.text.chunked(wrapWidth).forEach {
                                if (it.length == wrapWidth) {
                                    lines += listOf(Span.word(it, span.style))
                                } else {
                                    span = Span.word(it, span.style)
                                }
                            }
                        }
                    }
                }

                width += span.cellWidth
                line.add(span)

                lastPieceWasWhitespace = pieceIsWhitespace
            }

            if (!whitespace.collapseNewlines) {
                breakLine()
            }
        }

        if (line.isNotEmpty()) breakLine()

        return Lines(lines)
    }

    private fun lastNonWhitespace(line: Line, align: TextAlign): Int {
        return when {
            whitespace.trimEol || align == JUSTIFY -> line.indexOfLast { !it.isWhitespace() }
            else -> -1
        }
    }

    private fun alignLine(line: MutableList<Span>, wrapWidth: Int, width: Int, align: TextAlign): MutableList<Span> {
        val extraWidth = wrapWidth - width
        when (align) {
            LEFT -> alignLineLeft(line, extraWidth)
            RIGHT -> alignLineRight(line, extraWidth)
            CENTER -> alignLineCenter(line, extraWidth)
            JUSTIFY -> return justifyLine(line, extraWidth)
            NONE -> {
            }
        }
        return line
    }

    private fun alignLineLeft(line: MutableList<Span>, extraWidth: Int) {
        line.add(Span.space(extraWidth, line.lastOrNull()?.style ?: style))
    }

    private fun alignLineRight(line: MutableList<Span>, extraWidth: Int) {
        line.add(0, Span.space(extraWidth, line.firstOrNull()?.style ?: style))
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

internal fun Renderable.withAlign(align: TextAlign, overflowWrap: OverflowWrap? = null): Renderable {
    return if (this is Text) this.withAlign(align, overflowWrap) else this
}
