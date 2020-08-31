package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal

class Text(
        private val spans: List<Span>,
        private val styles: Set<TextStyle> = emptySet(),
        private val whitespace: Whitespace = Whitespace.NORMAL,
        private val align: TextAlign = TextAlign.LEFT
) : Renderable {
    constructor(text: String) : this(listOf(Span(text)))

    override fun measure(t: Terminal): IntRange {
        val plain = plain()
        val max = plain.lineSequence().maxOfOrNull { it.length } ?: 0
        val min = plain.split(WHTIESPACE_REGEX).minOfOrNull { it.length } ?: 0
        return min..max
    }

    override fun render(t: Terminal): List<Span> {
        return spans.map { it.withStyle(styles) } // TODO: wrap
    }

    private fun plain(): String = spans.joinToString("") { it.text }

    override fun toString(): String {
        val plain = plain()
        return "Text(${plain.take(25)}${if (plain.length > 25) "…" else ""})"
    }
}

private val WHTIESPACE_REGEX = Regex("\\S+")
