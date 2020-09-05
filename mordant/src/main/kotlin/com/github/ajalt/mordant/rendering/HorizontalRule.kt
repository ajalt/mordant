package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal
import com.github.ajalt.mordant.rendering.internal.parseText

class HorizontalRule internal constructor(
        private val ruleCharacter: String = "─",
        private val title: Line = emptyList(),
        private val ruleStyle: TextStyle? = null,
        private val titleStyle: TextStyle? = null
) : Renderable {
    constructor(
            ruleCharacter: String = "─",
            title: String = "",
            ruleStyle: TextStyle? = null,
            titleStyle: TextStyle? = null
    ) : this(
            ruleCharacter = ruleCharacter,
            title = parseText(title, TextStyle()).lines.firstOrNull() ?: emptyList(),
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
        val line = if (title.isEmpty()) {
            rule(t.theme, width)
        } else {
            val renderedTitle = title.map { it.withStyle(titleStyle ?: t.theme.horizontalRuleTitle) }
            val space = listOf(Span.word(" "))
            val remainingWidth = width - renderedTitle.sumOf { it.cellWidth } - 2
            val leftRule = rule(t.theme, remainingWidth / 2)
            val rightRule = rule(t.theme, remainingWidth / 2 + remainingWidth % 2)
            listOf(leftRule, space, renderedTitle, space, rightRule).flatten()
        }
        return Lines(listOf(line))
    }

    private fun rule(t: Theme, width: Int): List<Span> {
        val style = ruleStyle ?: t.horizontalRule
        val ruleWidth = width / ruleCharacter.length
        val rule = Span.word(ruleCharacter.repeat(ruleWidth), style)
        val remaining = width % ruleCharacter.length

        if (remaining == 0) return listOf(rule)

        val extraRule = Span.word(ruleCharacter.take(remaining), style)
        return listOf(rule, extraRule)
    }
}
