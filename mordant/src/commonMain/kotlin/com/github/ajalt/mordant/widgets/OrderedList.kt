package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.*
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.math.log10

/**
 * A numbered list of widgets.
 *
 * @property listEntries The content of the list
 * @property numberStyle The style of the list numbers. Defaults to the theme value `list.number`.
 * @property numberSeparator The string to print between the list numbers and the widgets. Defaults to the theme value
 *   `list.number.separator`
 */
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

    private fun sep(t: Theme): Line {
        val text = numberSeparator[t]
        require("\n" !in text) { "number separator cannot contain newlines" }
        return parseText(
            text,
            numberStyle[t]
        ).lines.firstOrNull() ?: EMPTY_LINE
    }

    private fun continuationPadding(i: Int, sepWidth: Int): List<Span> {
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
        val style = numberStyle[t]
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
                lines += Line(start + line)
            }
        }
        return Lines(lines)
    }
}

fun OrderedList(
    vararg listEntries: String,
    numberStyle: TextStyle? = null,
    numberSeparator: String? = null,
): OrderedList {
    return OrderedList(listEntries.map { Text(it) }, numberStyle, numberSeparator)
}

fun OrderedList(
    vararg listEntries: Widget,
    numberStyle: TextStyle? = null,
    numberSeparator: String? = null,
): OrderedList {
    return OrderedList(listEntries.toList(), numberStyle, numberSeparator)
}
