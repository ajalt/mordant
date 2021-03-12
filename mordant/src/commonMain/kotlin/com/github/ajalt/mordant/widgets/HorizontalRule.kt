package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.EMPTY_LINE
import com.github.ajalt.mordant.internal.ThemeDimension
import com.github.ajalt.mordant.internal.ThemeString
import com.github.ajalt.mordant.internal.ThemeStyle
import com.github.ajalt.mordant.internal.parseText
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal

class HorizontalRule internal constructor(
    private val title: Widget,
    private val ruleCharacter: ThemeString,
    private val ruleStyle: ThemeStyle,
    private val titleStyle: ThemeStyle?,
    private val titleAlign: TextAlign,
    private val titlePadding: ThemeDimension,
) : Widget {
    constructor(
        title: Widget = EmptyWidget,
        ruleCharacter: String? = null,
        ruleStyle: TextStyle? = null,
        titleAlign: TextAlign = TextAlign.CENTER,
        titlePadding: Int? = null,
    ) : this(
        title = title,
        ruleCharacter = ThemeString.of("hr.rule", ruleCharacter, " "),
        ruleStyle = ThemeStyle.of("hr.rule", ruleStyle),
        titleStyle = null,
        titleAlign = titleAlign,
        titlePadding = ThemeDimension.of("hr.title.padding", titlePadding),
    )

    constructor(
        title: String,
        ruleCharacter: String? = null,
        ruleStyle: TextStyle? = null,
        titleStyle: TextStyle? = null,
        titleAlign: TextAlign = TextAlign.CENTER,
        titlePadding: Int? = null,
    ) : this(
        title = if (title.isEmpty()) EmptyWidget else Text(titleStyle?.invoke(title) ?: title),
        ruleCharacter = ThemeString.of("hr.rule", ruleCharacter, " "),
        ruleStyle = ThemeStyle.of("hr.rule", ruleStyle),
        // titleStyle is applied by rendering the title widget and replacing the spans' styles. If
        // titleStyle isn't null here, we avoid the extra copies by passing it to the Text widget
        // we create above
        titleStyle = ThemeStyle.of("hr.title", null).takeIf { titleStyle == null },
        titleAlign = titleAlign,
        titlePadding = ThemeDimension.of("hr.title.padding", titlePadding),
    )

    override fun measure(t: Terminal, width: Int): WidthRange {
        return WidthRange(width, width)
    }

    override fun render(t: Terminal, width: Int): Lines {
        val padding = titlePadding[t]
        val totalPadding = 2 * padding
        val minBarWidth = 4 + totalPadding // 2 for each of left bar and right bar
        val content = title.withAlign(TextAlign.NONE).render(t, (width - minBarWidth).coerceAtLeast(0))
        val lines = if (content.isEmpty()) {
            listOf(rule(t, width))
        } else {
            val renderedTitle = content.withStyle(titleStyle?.let { it[t] })
            val lastLine = renderedTitle.lines.last()
            val ruleWidth = width - lastLine.sumOf { it.cellWidth } - totalPadding
            val leftRuleWidth = when (titleAlign) {
                TextAlign.LEFT -> 1
                TextAlign.RIGHT -> ruleWidth - 1
                TextAlign.CENTER,
                TextAlign.JUSTIFY,
                TextAlign.NONE,
                -> ruleWidth / 2
            }
            val leftRule = rule(t, leftRuleWidth)
            val rightRule = rule(t, ruleWidth - leftRuleWidth)
            val space = if (padding > 0) Span.space(padding, ruleStyle[t]) else null
            val rule = flatLine(leftRule, space, lastLine, space, rightRule)
            if (renderedTitle.lines.size > 1) {
                val firstLines = Lines(renderedTitle.lines.dropLast(1))
                    .setSize(width, textAlign = TextAlign.CENTER)
                firstLines.lines + listOf(rule)
            } else {
                listOf(rule)
            }
        }
        return Lines(lines)
    }

    private fun rule(t: Terminal, width: Int): Line {
        if (width <= 0) return EMPTY_LINE
        val style = ruleStyle[t]
        val c = ruleCharacter[t]

        require("\n" !in c) { "Rule characters cannot contain line breaks" }
        require(c.isNotEmpty()) { "Rule characters cannot be empty." }

        val ruleWidth = width / c.length
        val rule = parseText(c.repeat(ruleWidth), style).lines.single()
        val remaining = width % c.length

        if (remaining == 0) return rule

        val extraRule = Span.word(c.take(remaining), style)
        return Line(rule + extraRule)
    }
}
