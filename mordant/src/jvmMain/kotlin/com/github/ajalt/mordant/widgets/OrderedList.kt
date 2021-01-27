package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.ThemeString
import com.github.ajalt.mordant.internal.ThemeStyle
import com.github.ajalt.mordant.internal.parseText
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.math.log10

class OrderedList private constructor(
    private val listEntries: List<Widget>,
    private val numberStyle: ThemeStyle,
    private val numberSeparator: ThemeString,
) : Widget {
    constructor(
        listEntries: List<Widget>,
        numberStyle: TextStyle? = null,
        numberSeparator: String? = null,
    ) : this(
        listEntries,
        ThemeStyle.of("list.number", numberStyle),
        ThemeString.of("list.number.separator", numberSeparator)
    )

    init {
        require(listEntries.isNotEmpty()) { "Cannot render an empty list" }
    }

    private fun sep(t: Theme): Line {
        val text = numberSeparator[t]
        require("\n" !in text) { "number separator cannot contain newlines" }
        return parseText(
            text,
            numberStyle[t]
        ).lines.firstOrNull() ?: EMPTY_LINE
    }

    private fun continuationPadding(i: Int, sepWidth: Int): Line {
        val n = bulletWidth(i, sepWidth)
        return listOf(Span.space(n))
    }

    private fun bulletWidth(i: Int, sepWidth: Int): Int {
        return (log10((i + 1).toDouble()).toInt() + 1 // number
                + 2 // padding
                + sepWidth
                )
    }

    private val maxBulletWidth = bulletWidth(listEntries.size, sep(Theme.Default).lineWidth)

    override fun measure(t: Terminal, width: Int): WidthRange {
        return listEntries.maxWidthRange(t, width, maxBulletWidth)
    }

    override fun render(t: Terminal, width: Int): Lines {
        val contentWidth = width - maxBulletWidth
        val lines = mutableListOf<Line>()
        val style = numberStyle[t.theme]
        val sep = sep(t.theme)
        val sepWidth = sep.lineWidth

        for ((i, entry) in listEntries.withIndex()) {
            val bullet = flatLine(
                SINGLE_SPACE,
                Span.word("${i + 1}", style),
                sep,
                SINGLE_SPACE
            )
            for ((j, line) in entry.render(t, contentWidth).lines.withIndex()) {
                val start = if (j == 0) bullet else continuationPadding(i, sepWidth)
                lines += start + line
            }
        }
        return Lines(lines)
    }
}
