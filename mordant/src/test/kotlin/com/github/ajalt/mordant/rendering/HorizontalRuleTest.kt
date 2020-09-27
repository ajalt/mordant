package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal
import org.junit.Test

class HorizontalRuleTest : RenderingTest() {
    @Test
    fun `no title`() {
        t = Terminal(width = 10)
        checkRender(HorizontalRule(), "──────────")
    }

    @Test
    fun `multiple character rules`() {
        t = Terminal(width = 20)
        checkRender(HorizontalRule(title = "title", ruleCharacter = "1234"), "123412 title 1234123")
    }

    @Test
    fun `rule with whitespace`() {
        t = Terminal(width = 6)
        checkRender(HorizontalRule(ruleCharacter = "- -"), "- -- -")
    }

    @Test
    fun `multiline title`() {
        t = Terminal(width = 19)
        checkRender(HorizontalRule(title = Text("Multiline\nHeader Text", whitespace = Whitespace.PRE_WRAP)),
                """
                     Multiline     ⏎
                ─── Header Text ───⏎
                """
        )
    }
}
