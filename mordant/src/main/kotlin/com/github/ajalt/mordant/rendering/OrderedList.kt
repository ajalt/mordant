package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal
import kotlin.math.log10

class OrderedList(
        private val listEntries: List<Renderable>,
        private val numberStyle: TextStyle? = null
) : Renderable {
    init {
        require(listEntries.isNotEmpty()) { "Cannot render an empty list" }
    }

    private fun bullet(i: Int, t: Theme): Line {
        val style = numberStyle ?: t.listNumber
        val padding = Span.word(" ", style)
        return listOf(padding, Span.word("${i + 1}.", style), padding)
    }

    private fun continuationPadding(i: Int, t: Theme): Line {
        val n = bulletWidth(i)
        return listOf(Span.word(" ".repeat(n), numberStyle ?: t.listNumber))
    }

    private fun bulletWidth(i: Int): Int {
        return (log10((i + 1).toDouble()).toInt() + 1 // number
                + 2 // padding
                + 1 // dot
                )
    }

    private val maxBulletWidth = bulletWidth(listEntries.size)

    override fun measure(t: Terminal, width: Int): WidthRange {
        return listEntries.maxWidthRange(t, width, maxBulletWidth)
    }

    override fun render(t: Terminal, width: Int): Lines {
        val contentWidth = width - maxBulletWidth
        val lines = mutableListOf<Line>()

        for ((i, entry) in listEntries.withIndex()) {
            val bullet = bullet(i, t.theme)
            for ((j, line) in entry.render(t, contentWidth).lines.withIndex()) {
                val start = if (j == 0) bullet else continuationPadding(i, t.theme)
                lines += start + line
            }
        }
        return Lines(lines)
    }
}
