package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.rendering.TextAlign.*

typealias Line = List<Span>


data class Lines(
        val lines: List<Line>,
) {
    val size: Int get() = lines.size

    internal fun withStyle(style: TextStyle?): Lines {
        return when (style) {
            null, DEFAULT_STYLE -> this
            else -> Lines(lines.map { l -> l.map { it.withStyle(style) } })
        }
    }

    internal operator fun plus(other: Lines): Lines {
        return when {
            lines.isEmpty() -> other
            other.lines.isEmpty() -> this
            else -> {
                Lines(listOf(
                        lines.dropLast(1),
                        listOf(lines.last() + other.lines.first()),
                        other.lines.drop(1)
                ).flatten())
            }
        }
    }
}

/** Pad or crop every line so its width is exactly [newWidth] */
internal fun Lines.setSize(newWidth: Int, align: TextAlign, height: Int? = null): Lines {
    val lines = mutableListOf<Line>()
    for (line in this.lines) {
        var width = 0
        for ((j, span) in line.withIndex()) {
            when {
                width + span.cellWidth <= newWidth -> {
                    width += span.cellWidth

                }
                width == newWidth -> {
                    lines.add(line.subList(0, j))
                    break
                }
                else -> {
                    lines.add(line.subList(0, j) + span.take(newWidth - width))
                    width = newWidth
                    break
                }
            }
        }

        val remainingWidth = newWidth - width
        if (remainingWidth > 0) {
            when (align) {
                CENTER, JUSTIFY -> {
                    val l = Span.space(remainingWidth / 2)
                    val r = Span.space(remainingWidth / 2 + remainingWidth % 2)
                    lines.add(listOf(listOf(l), line, listOf(r)).flatten())
                }
                LEFT -> {
                    lines.add(line + Span.space(remainingWidth))
                }
                RIGHT -> {
                    lines.add(listOf(Span.space(remainingWidth)) + line)
                }
            }
        } else {
            lines.add(line)
        }
    }

    if (height != null && height != lines.size) {
        // TODO vertical align
        if (height < lines.size) {
            return Lines(lines.take(height))
        } else {
            val line = listOf(Span.space(newWidth))
            repeat(height - lines.size) {
                lines.add(line)
            }
        }
    }
    return Lines(lines)
}

internal val EMPTY_LINES = Lines(emptyList())
