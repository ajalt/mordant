package com.github.ajalt.mordant.rendering.components

import com.github.ajalt.mordant.components.HorizontalRule
import com.github.ajalt.mordant.components.Text
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.TextColors.red
import org.junit.Test

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
    fun `title align left`() {
        checkRender(HorizontalRule("title", titleAlign = TextAlign.LEFT), "─ title ────", width = 12)
    }

    @Test
    fun `title align right`() {
        checkRender(HorizontalRule("title", titleAlign = TextAlign.RIGHT), "──── title ─", width = 12)
    }

    @Test
    fun `multiline title`() {
        checkRender(
            HorizontalRule(title = Text("Multiline\nHeader Text", whitespace = Whitespace.PRE_WRAP)),
            """
                 Multiline     ⏎
            ─── Header Text ───⏎
            """,
            width = 19
        )
    }

    @Test
    fun `styled title and rule`() {
        checkRender(
            HorizontalRule(title = "title", ruleStyle = TextStyle(red), titleStyle = TextStyle(red)),
            red("─── title ───"), width = 13
        )
    }

    @Test
    fun `themed title and rule`() {
        checkRender(
            HorizontalRule(title = "title"),
            red("──  title  ──"),
            width = 13,
            theme = Theme {
                styles["hr.rule"] = red
                styles["hr.title"] = red
                dimensions["hr.title.padding"] = 2
            }
        )
    }
}
