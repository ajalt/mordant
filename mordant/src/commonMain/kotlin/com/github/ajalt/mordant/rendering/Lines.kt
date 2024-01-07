package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.EMPTY_LINE
import com.github.ajalt.mordant.internal.EMPTY_LINES
import com.github.ajalt.mordant.rendering.TextAlign.*
import com.github.ajalt.mordant.rendering.VerticalAlign.*

data class Line(val spans: List<Span>, val endStyle: TextStyle) : List<Span> by spans {
    constructor(spans: List<Span>) : this(spans, spans.lastOrNull()?.style ?: DEFAULT_STYLE)
}

/**
 * A lines, where each line is a list of [Span]s.
 *
 * Linebreaks are implicit between lines. There is no trailing line break. Lines can be empty.
 */
class Lines(
    val lines: List<Line>,
) {
    val height: Int get() = lines.size
    val width: Int get() = lines.maxOfOrNull { it.lineWidth } ?: 0

    fun isEmpty(): Boolean = lines.isEmpty()

    /**
     * Create a copy of these lines with [style] combined with any existing style
     */
    fun withStyle(style: TextStyle?): Lines {
        return when (style) {
            null, DEFAULT_STYLE -> this
            else -> Lines(
                lines.map { l -> Line(l.map { it.withStyle(style) }, l.endStyle + style) }
            )
        }
    }

    /**
     * Create a copy of these lines with any existing styles replaced with [style]
     */
    fun replaceStyle(style: TextStyle?): Lines {
        return when (style) {
            null, DEFAULT_STYLE -> this
            else -> Lines(lines.map { l -> Line(l.map { it.replaceStyle(style) }, style) })
        }
    }
}

internal val Line.lineWidth: Int
    get() = spans.sumOf { it.cellWidth }

/** Equivalent to `listOf(...).flatten(), but ignores nulls and doesn't require wrapping single items in a list */
internal fun flatLine(vararg parts: Any?): Line {
    val size = parts.sumOf { if (it is Collection<*>) it.size else 1 }
    val line = ArrayList<Span>(size)
    for (part in parts) {
        when (part) {
            null -> {
            }
            is Collection<*> -> part.mapTo(line) { it as Span }
            is Span -> line.add(part)
            else -> error("not a span: $part")
        }
    }
    return Line(line)
}

/**
 * Pad or crop every line so its width is exactly [newWidth], and add or remove lines so its height
 * is exactly [newHeight].
 *
 * If [newWidth] is null, the width if each line will be unchanged.
 */
internal fun Lines.setSize(
    newWidth: Int?,
    newHeight: Int = lines.size,
    verticalAlign: VerticalAlign = TOP,
    textAlign: TextAlign = NONE,
): Lines {
    if (newHeight == 0) return EMPTY_LINES
    if (newWidth == 0) return Lines(List(newHeight) { EMPTY_LINE })
    if (newWidth == null) {
        return if (newHeight == lines.size) this
        else if (newHeight < lines.size ) Lines(lines.take(newHeight))
        else Lines(lines + List(newHeight - lines.size) { EMPTY_LINE })
    }

    val heightToAdd = (newHeight - lines.size).coerceAtLeast(0)

    val emptyLine = Line(listOf(Span.space(newWidth)))
    val lines = ArrayList<Line>(newHeight)

    val topEmptyLines = when (verticalAlign) {
        TOP -> 0
        MIDDLE -> heightToAdd / 2 + heightToAdd % 2
        BOTTOM -> heightToAdd
    }

    repeat(topEmptyLines) {
        lines.add(emptyLine)
    }

    line@ for ((i, line) in this.lines.withIndex()) {
        if (i >= newHeight) break

        var width = 0
        for ((j, span) in line.withIndex()) {
            when {
                width + span.cellWidth <= newWidth -> {
                    width += span.cellWidth
                }
                width == newWidth -> {
                    lines.add(Line(line.subList(0, j)))
                    continue@line
                }
                else -> {
                    lines.add(Line(line.subList(0, j) + span.take(newWidth - width)))
                    continue@line
                }
            }
        }

        val remainingWidth = newWidth - width
        if (remainingWidth > 0) {
            val beginStyle = line.firstOrNull()?.style ?: line.endStyle
            val endStyle = line.endStyle

            when (textAlign) {
                CENTER, JUSTIFY -> {
                    val l = Span.space(remainingWidth / 2, beginStyle)
                    val r = Span.space(remainingWidth / 2 + remainingWidth % 2, endStyle)
                    lines.add(Line(listOf(listOf(l), line, listOf(r)).flatten()))
                }
                LEFT -> {
                    lines.add(Line(line + Span.space(remainingWidth, endStyle)))
                }
                NONE -> {
                    lines.add(Line(line + Span.space(remainingWidth))) // Spaces aren't styled in this alignment
                }
                RIGHT -> {
                    lines.add(Line(listOf(Span.space(remainingWidth, beginStyle)) + line))
                }
            }
        } else {
            lines.add(line)
        }
    }

    if (newHeight != lines.size) {
        if (newHeight < lines.size) {
            return Lines(lines.take(newHeight))
        } else {
            val line = if (newWidth == 0) EMPTY_LINE else Line(listOf(Span.space(newWidth)))
            repeat(newHeight - lines.size) {
                lines.add(line)
            }
        }
    }
    return Lines(lines)
}
