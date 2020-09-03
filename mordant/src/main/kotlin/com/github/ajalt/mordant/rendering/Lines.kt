package com.github.ajalt.mordant.rendering

typealias Line = List<Span>


data class Lines(
        val lines: List<Line>,
) {
    init {
        for (line in lines) {
            for (span in line) {
                require(!span.text.endsWith("\n"))
            }
        }
    }

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


internal fun List<Span>.toLines(): MutableList<Line> {
    val lines = mutableListOf<Line>()
    var start = 0
    var i = 0
    while (i <= lastIndex) {
        if (get(i).text.endsWith("\n")) {
            lines.add(subList(start, i + 1))
            start = i
        }
        i += 1
    }
    if (start < lastIndex) lines.add(subList(start, size))
    if (lines.isNotEmpty() && !lines.last().last().text.endsWith("\n")) {
        lines[lines.lastIndex] = lines.last() + lines.last().last().copy(text = "\n")
    }
    return lines
}


//fun Lines.setWidth(width: Int) {
//    for ((i, line) in withIndex()) {
//        var w = 0
//        for ((j, span) in line.withIndex()) {
//            when {
//                w + span.cellWidth <= width -> {
//                    w += span.cellWidth
//                }
//                w == width -> {
//                    set(i, line.subList(0, j))
//                    break
//                }
//                else -> {
//                    set(i, line.subList(0, j) + span.take(width - w))
//                    w = width
//                    break
//                }
//            }
//        }
//
//        if (w < width) {
//            // insert padding before trailing newline
//            set(i, line.dropLast(1) + listOf(Span.word(" ".repeat(width - w)), line.last()))
//        }
//    }
//}
