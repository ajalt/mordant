package com.github.ajalt.mordant.components

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.internal.parseText
import kotlin.math.log10

class OrderedList(
        private val listEntries: List<Renderable>,
        private val numberStyle: TextStyle? = null,
        private val numberSeparator: String? = null
) : Renderable {
    init {
        require(listEntries.isNotEmpty()) { "Cannot render an empty list" }
    }

    private fun sep(t: Theme): Line {
        val text = numberSeparator ?: t.listNumberSeparator
        require("\n" !in text) { "number separator cannot contain newlines" }
        return parseText(
                text,
                numberStyle ?: t.listNumber
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

    private val maxBulletWidth = bulletWidth(listEntries.size, sep(DEFAULT_THEME).lineWidth)

    override fun measure(t: Terminal, width: Int): WidthRange {
        return listEntries.maxWidthRange(t, width, maxBulletWidth)
    }

    override fun render(t: Terminal, width: Int): Lines {
        val contentWidth = width - maxBulletWidth
        val lines = mutableListOf<Line>()
        val style = numberStyle ?: t.theme.listNumber
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
