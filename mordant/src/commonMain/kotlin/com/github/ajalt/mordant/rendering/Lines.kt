package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.EMPTY_LINE
import com.github.ajalt.mordant.internal.EMPTY_LINES
import com.github.ajalt.mordant.rendering.TextAlign.*
import com.github.ajalt.mordant.rendering.VerticalAlign.*

data class Line(val spans: List<Span>, val endStyle: TextStyle) : List<Span> by spans {
    constructor(spans: List<Span>) : this(spans, spans.lastOrNull()?.style ?: DEFAULT_STYLE)
}

internal val Line.startStyle: TextStyle get() = firstOrNull()?.style ?: DEFAULT_STYLE

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
    newWidth: Int,
    newHeight: Int = lines.size,
    verticalAlign: VerticalAlign = TOP,
    textAlign: TextAlign = NONE,
    scrollRight: Int = 0,
    scrollDown: Int = 0,
): Lines {
    if (newHeight <= 0) return EMPTY_LINES
    if (newWidth <= 0) return Lines(List(newHeight) { EMPTY_LINE })
    val emptyLine = Line(listOf(Span.space(newWidth)))

    val offsetLines = when {
        scrollDown == 0 -> lines
        scrollDown !in -lines.lastIndex..lines.lastIndex -> emptyList()
        scrollDown < 0 -> buildList(lines.size - scrollDown) {
            repeat(-scrollDown) { add(emptyLine) }
            addAll(lines)
        }

        else -> lines.subList(scrollDown, lines.size)
    }

    val heightToAdd = (newHeight - offsetLines.size).coerceAtLeast(0)
    val topEmptyLineCount = when (verticalAlign) {
        TOP -> 0
        MIDDLE -> heightToAdd / 2 + heightToAdd % 2
        BOTTOM -> heightToAdd
    }

    return Lines(buildList(newHeight) {
        repeat(topEmptyLineCount) { add(emptyLine) }
        offsetLines.subList(0, newHeight.coerceAtMost(offsetLines.size)).mapTo(this) {
            Line(resizeLine(it, scrollRight, newWidth, textAlign))
        }
        repeat(newHeight - topEmptyLineCount - offsetLines.size) { add(emptyLine) }
    })
}

private fun resizeLine(
    line: Line, scrollRight: Int, newWidth: Int, textAlign: TextAlign,
): List<Span> {
    var width = 0
    var offset = 0
    var offsetLine = line.spans
    if (scrollRight < 0) {
        offsetLine = listOf(Span.space(-scrollRight, line.startStyle)) + offsetLine
    }
    for ((j, span) in offsetLine.withIndex()) {
        when {
            // If we have an offset, skip spans until we reach it
            scrollRight > 0 && offset + span.cellWidth < scrollRight -> {
                offset += span.cellWidth
                offsetLine = line.subList(j + 1, line.size)
            }

            // If we have an offset, and this span is the one that contains it, split it
            scrollRight > 0 && offset < scrollRight -> {
                offsetLine = line.subList(j + 1, line.size)
                if (offset + span.cellWidth > scrollRight) {
                    val splitSpan = span.drop(scrollRight - offset)
                    offsetLine = listOf(splitSpan) + offsetLine
                    width += splitSpan.cellWidth
                }
                offset = scrollRight
            }

            // We're past the offset, so add spans until we reach the new width
            width + span.cellWidth <= newWidth -> {
                width += span.cellWidth
            }

            // We've reached the new width before the end of the line, so make a new line
            else -> {
                val l = when {
                    width == newWidth -> offsetLine.subList(0, j)
                    else -> offsetLine.subList(0, j) + span.take(newWidth - width)
                }
                return l
            }
        }
    }

    // We didn't truncate the line
    val remainingWidth = newWidth - width

    if (remainingWidth == 0) {
        // The line is exactly the right width
        return offsetLine
    }

    val beginStyle = offsetLine.firstOrNull()?.style ?: line.endStyle
    val endStyle = line.endStyle

    // The line was too short, add spaces according to the alignment
    return when (textAlign) {
        CENTER, JUSTIFY -> {
            val l = Span.space(remainingWidth / 2, beginStyle)
            val r = Span.space(remainingWidth / 2 + remainingWidth % 2, endStyle)
            buildList(offsetLine.size + 2) { add(l); addAll(offsetLine); add(r) }
        }

        LEFT -> offsetLine + Span.space(remainingWidth, endStyle)
        NONE -> offsetLine + Span.space(remainingWidth) // Spaces aren't styled in this alignment
        RIGHT -> listOf(Span.space(remainingWidth, beginStyle)) + offsetLine
    }
}
