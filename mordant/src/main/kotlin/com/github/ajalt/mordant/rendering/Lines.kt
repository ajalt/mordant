package com.github.ajalt.mordant.rendering

typealias Line = List<Span>


data class Lines(
        val lines: List<Line>,
) {
    internal fun withStyle(style: TextStyle) = Lines(lines.map { l -> l.map { it.withStyle(style) } })

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
internal fun Lines.setWidth(newWidth: Int): Lines {
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

        if (width < newWidth) {
            lines.add(line + Span.word(" ".repeat(newWidth - width)))
        } else {
            lines.add(line)
        }
    }
    return Lines(lines)
}
