package com.github.ajalt.mordant.widgets

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
        title = if (title.isEmpty()) EmptyWidget else Text(title, titleStyle ?: DEFAULT_STYLE),
        ruleCharacter =  ThemeString.of("hr.rule", ruleCharacter, " "),
        ruleStyle = ThemeStyle.of("hr.rule", ruleStyle),
        // The explicit style is baked in to the Text object, so only override the rendered style if
        // we need it from the theme.
        titleStyle = ThemeStyle.of("hr.title", null).takeIf { titleStyle == null },
        titleAlign = titleAlign,
        titlePadding = ThemeDimension.of("hr.title.padding", titlePadding),
    )

    override fun measure(t: Terminal, width: Int): WidthRange {
        return WidthRange(width, width)
    }

    override fun render(t: Terminal, width: Int): Lines {
        val padding = titlePadding[t.theme]
        val totalPadding = 2 * padding
        val minBarWidth = 4 + totalPadding // 2 for each of left bar and right bar
        val content = title.withAlign(TextAlign.NONE).render(t, (width - minBarWidth).coerceAtLeast(0))
        val lines = if (content.isEmpty()) {
            listOf(rule(t.theme, width))
        } else {
            val renderedTitle = content.withStyle(titleStyle?.let { it[t.theme] })
            val lastLine = renderedTitle.lines.last()
            val ruleWidth = width - lastLine.sumOf { it.cellWidth } - totalPadding
            val leftRuleWidth = when (titleAlign) {
                TextAlign.LEFT -> 1
                TextAlign.RIGHT -> ruleWidth - 1
                TextAlign.CENTER,
                TextAlign.JUSTIFY,
                TextAlign.NONE -> ruleWidth / 2
            }
            val leftRule = rule(t.theme, leftRuleWidth)
            val rightRule = rule(t.theme, ruleWidth - leftRuleWidth)
            val space = if (padding > 0) Span.space(padding, ruleStyle[t.theme]) else null
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

    private fun rule(t: Theme, width: Int): Line {
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
        return rule + extraRule
    }
}
