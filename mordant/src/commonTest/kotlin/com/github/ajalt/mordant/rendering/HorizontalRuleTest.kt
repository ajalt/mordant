package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.components.HorizontalRule
import com.github.ajalt.mordant.components.Text
import com.github.ajalt.mordant.terminal.TextColors.red
import kotlin.test.Test

class HorizontalRuleTest : RenderingTest() {
    @Test
    fun `no title`() {
        checkRender(HorizontalRule(), "──────────", width = 10)
    }

    @Test
    fun `multiple character rules`() {
        checkRender(HorizontalRule(title = "title", ruleCharacter = "1234"), "123412 title 1234123", width = 20)
    }

    @Test
    fun `rule with whitespace`() {
        checkRender(HorizontalRule(ruleCharacter = "- -"), "- -- -", width = 6)
    }

    @Test
    fun `multiline title`() {
        checkRender(HorizontalRule(title = Text("Multiline\nHeader Text", whitespace = Whitespace.PRE_WRAP)),
                """
                     Multiline     ⏎
                ─── Header Text ───⏎
                """,
                width = 19
        )
    }

    @Test
    fun `styled title and rule`() {
        checkRender(HorizontalRule(title = "title", ruleStyle = TextStyle(red), titleStyle = TextStyle(red)),
                red("─── title ───"), width = 13)
    }
}
