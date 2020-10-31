package com.github.ajalt.mordant.components

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.TextAlign.NONE
import com.github.ajalt.mordant.internal.parseText

class HorizontalRule internal constructor(
        private val title: Renderable,
        private val ruleCharacter: String = "─",
        private val ruleStyle: TextStyle? = null,
        private val titleStyle: TextStyle? = null
) : Renderable {
    constructor() : this(EmptyRenderable)
    constructor(
            title: String = "",
            ruleCharacter: String = "─",
            ruleStyle: TextStyle? = null,
            titleStyle: TextStyle? = null
    ) : this(
            title = Text(parseText(title, DEFAULT_STYLE)),
            ruleCharacter = ruleCharacter,
            ruleStyle = ruleStyle,
            titleStyle = titleStyle
    )

    init {
        require("\n" !in ruleCharacter) { "Rule characters cannot contain line breaks" }
        require(ruleCharacter.isNotEmpty()) { "Rule characters cannot be empty." }
    }

    override fun measure(t: Terminal, width: Int): WidthRange {
        return WidthRange(width, width)
    }

    override fun render(t: Terminal, width: Int): Lines {
        val minBarWidth = 6 // 2 for each of left bar, right bar, padding
        val content = title.withAlign(NONE).render(t, width - minBarWidth)
        val lines = if (content.lines.isEmpty()) {
            listOf(rule(t.theme, width))
        } else {
            val renderedTitle = content.withStyle(this.titleStyle ?: t.theme.horizontalRuleTitle)
            val lastLine = renderedTitle.lines.last()
            val ruleWidth = width - lastLine.sumOf { it.cellWidth } - 2 // -2 for padding
            val leftRule = rule(t.theme, ruleWidth / 2)
            val rightRule = rule(t.theme, ruleWidth / 2 + ruleWidth % 2)
            val space = SINGLE_SPACE.withStyle(ruleStyle ?: t.theme.horizontalRule)
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
        val style = ruleStyle ?: t.horizontalRule
        val ruleWidth = width / ruleCharacter.length
        val rule = parseText(ruleCharacter.repeat(ruleWidth), style).lines.single()
        val remaining = width % ruleCharacter.length

        if (remaining == 0) return rule

        val extraRule = Span.word(ruleCharacter.take(remaining), style)
        return rule + extraRule
    }
}
