package com.github.ajalt.mordant.components

import com.github.ajalt.mordant.internal.ThemeString
import com.github.ajalt.mordant.internal.ThemeStyle
import com.github.ajalt.mordant.internal.parseText
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal

class HorizontalRule private constructor(
    private val title: Renderable,
    private val ruleCharacter: ThemeString,
    private val ruleStyle: ThemeStyle,
    private val titleStyle: ThemeStyle,
    private val titleAlign: TextAlign = TextAlign.CENTER,
) : Renderable {
    constructor(
        title: Renderable = EmptyRenderable,
        ruleCharacter: String? = null,
        ruleStyle: TextStyle? = null,
        titleStyle: TextStyle? = null,
        titleAlign: TextAlign = TextAlign.CENTER,
    ) : this(
        title = title,
        ruleCharacter = ThemeString.of("hr.rule", ruleCharacter, " "),
        ruleStyle = ThemeStyle.of("hr.rule", ruleStyle),
        titleStyle = ThemeStyle.of("hr.title", titleStyle),
        titleAlign = titleAlign
    )

    constructor(
        title: String,
        ruleCharacter: String? = null,
        ruleStyle: TextStyle? = null,
        titleStyle: TextStyle? = null,
        titleAlign: TextAlign = TextAlign.CENTER,
    ) : this(
        title = if (title.isEmpty()) EmptyRenderable else Text(parseText(title, DEFAULT_STYLE)),
        ruleCharacter = ruleCharacter,
        ruleStyle = ruleStyle,
        titleStyle = titleStyle,
        titleAlign = titleAlign
    )

    override fun measure(t: Terminal, width: Int): WidthRange {
        return WidthRange(width, width)
    }

    override fun render(t: Terminal, width: Int): Lines {
        val minBarWidth = 6 // 2 for each of left bar, right bar, padding
        val content = title.withAlign(TextAlign.NONE).render(t, (width - minBarWidth).coerceAtLeast(0))
        val lines = if (content.isEmpty()) {
            listOf(rule(t.theme, width))
        } else {
            val renderedTitle = content.withStyle(titleStyle[t.theme])
            val lastLine = renderedTitle.lines.last()
            val ruleWidth = width - lastLine.sumOf { it.cellWidth } - 2 // -2 for padding
            val leftRuleWidth = when (titleAlign) {
                TextAlign.LEFT -> 1
                TextAlign.RIGHT -> ruleWidth - 1
                TextAlign.CENTER,
                TextAlign.JUSTIFY,
                TextAlign.NONE -> ruleWidth / 2
            }
            val leftRule = rule(t.theme, leftRuleWidth)
            val rightRule = rule(t.theme, ruleWidth - leftRuleWidth)
            val space = SINGLE_SPACE.withStyle(ruleStyle[t.theme])
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
