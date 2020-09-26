package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal
import com.github.ajalt.mordant.rendering.internal.parseText

class HorizontalRule internal constructor(
        private val ruleCharacter: String = "─",
        private val title: Renderable = EMPTY_RENDERABLE,
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
            title = Text(parseText(title, DEFAULT_STYLE)),
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
        val content = title.render(t, width - minBarWidth)
        val lines = if (content.lines.isEmpty()) {
            listOf(rule(t.theme, width))
        } else {
            val renderedTitle = Lines(content.lines.map { l ->
                l.map {
                    it.withStyle(titleStyle ?: t.theme.horizontalRuleTitle)
                }
            })
            val lastLine = renderedTitle.lines.last()
            val space = listOf(SINGLE_SPACE)
            val remainingWidth = width - lastLine.sumOf { it.cellWidth } - 2
            val leftRule = rule(t.theme, remainingWidth / 2)
            val rightRule = rule(t.theme, remainingWidth / 2 + remainingWidth % 2)
            val rule = listOf(leftRule, space, lastLine, space, rightRule).flatten()
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

    private fun rule(t: Theme, width: Int): List<Span> {
        val style = ruleStyle ?: t.horizontalRule
        val ruleWidth = width / ruleCharacter.length
        val rule = parseText(ruleCharacter.repeat(ruleWidth), style).lines.single()
        val remaining = width % ruleCharacter.length

        if (remaining == 0) return rule

        val extraRule = Span.word(ruleCharacter.take(remaining), style)
        return rule + extraRule
    }
}
