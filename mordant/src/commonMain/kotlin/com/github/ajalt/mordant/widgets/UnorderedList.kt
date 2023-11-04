package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.*
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal

class UnorderedList private constructor(
    private val listEntries: List<Widget>,
    private val bulletText: ThemeString,
    private val bulletStyle: ThemeStyle,
) : Widget {
    constructor(
        listEntries: List<Widget>,
        bulletText: String? = null,
        bulletStyle: TextStyle? = null,
    ) : this(
        listEntries,
        ThemeString.of("list.bullet.text", bulletText),
        ThemeStyle.of("list.bullet", bulletStyle)
    )

    private fun bullet(t: Theme): Line {
        val text = bulletText[t]
        require("\n" !in text) { "bullet text cannot contain newlines" }
        if (text.isEmpty()) return EMPTY_LINE
        return flatLine(
            SINGLE_SPACE,
            parseText(text, bulletStyle[t]).lines.firstOrNull() ?: EMPTY_LINE,
            SINGLE_SPACE
        )
    }

    override fun measure(t: Terminal, width: Int): WidthRange {
        val bulletWidth = bullet(t.theme).sumOf { it.cellWidth }
        return listEntries.maxWidthRange(t, width, bulletWidth)
    }

    override fun render(t: Terminal, width: Int): Lines {
        val bullet = bullet(t.theme)
        val bulletWidth = bullet.sumOf { it.cellWidth }
        val contentWidth = (width - bulletWidth).coerceAtLeast(0)
        val continuationPadding = when {
            bulletWidth > 0 -> listOf(Span.space(bulletWidth, bulletStyle[t.theme]))
            else -> EMPTY_LINE
        }

        val lines = mutableListOf<Line>()

        for (entry in listEntries) {
            for ((i, line) in entry.render(t, contentWidth).lines.withIndex()) {
                val start = if (i == 0) bullet else continuationPadding
                lines += Line(start + line)
            }
        }
        return Lines(lines)
    }
}

fun UnorderedList(
    vararg listEntries: String,
    bulletText: String? = null,
    bulletStyle: TextStyle? = null,
): UnorderedList = UnorderedList(
    listEntries.map { Text(it) },
    bulletText,
    bulletStyle,
)

fun UnorderedList(
    vararg listEntries: Widget,
    bulletText: String? = null,
    bulletStyle: TextStyle? = null,
): UnorderedList = UnorderedList(
    listEntries.toList(),
    bulletText,
    bulletStyle,
)
