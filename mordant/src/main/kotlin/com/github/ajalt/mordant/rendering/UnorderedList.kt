package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal
import com.github.ajalt.mordant.rendering.internal.parseText

class UnorderedList(
        private val listEntries: List<Renderable>,
        private val bulletText: String? = null,
        private val bulletStyle: TextStyle? = null
) : Renderable {
    init {
        require(listEntries.isNotEmpty()) { "Cannot render an empty list" }
    }

    private fun bullet(t: Theme): Line {
        val text = bulletText ?: t.listBulletText
        require("\n" !in text) { "bullet text cannot contain newlines" }
        return parseText(text, bulletStyle ?: t.listBullet).lines.singleOrNull() ?: emptyList()
    }

    override fun measure(t: Terminal, width: Int): WidthRange {
        val bulletWidth = bullet(t.theme).sumOf { it.cellWidth }
        return listEntries.maxWidthRange(t, width, bulletWidth)
    }

    override fun render(t: Terminal, width: Int): Lines {
        val bullet = bullet(t.theme)
        val bulletWidth = bullet.sumOf { it.cellWidth }
        val contentWidth = width - bulletWidth
        val continuationPadding = when {
            bulletWidth > 0 -> listOf(Span.word(" ".repeat(bulletWidth), bulletStyle ?: t.theme.listBullet))
            else -> emptyList()
        }

        val lines = mutableListOf<Line>()

        for (entry in listEntries) {
            for ((i, line) in entry.render(t, contentWidth).lines.withIndex()) {
                val start = if (i == 0) bullet else continuationPadding
                lines += start + line
            }
        }
        return Lines(lines)
    }
}
