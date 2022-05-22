package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors.blue
import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.rendering.Whitespace
import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.widgets.HorizontalRule
import com.github.ajalt.mordant.widgets.Text
import kotlin.js.JsName
import kotlin.test.Test

class HorizontalRuleTest : RenderingTest() {
    @Test
    @JsName("no_title")
    fun `no title`() {
        checkRender(HorizontalRule(), "──────────", width = 10)
    }

    @Test
    @JsName("multiple_character_rules")
    fun `multiple character rules`() {
        checkRender(HorizontalRule(title = "title", ruleCharacter = "1234"), "123412 title 1234123", width = 20)
    }

    @Test
    @JsName("rule_with_whitespace")
    fun `rule with whitespace`() {
        checkRender(HorizontalRule(ruleCharacter = "- -"), "- -- -", width = 6)
    }

    @Test
    @JsName("title_align_left")
    fun `title align left`() {
        checkRender(HorizontalRule("title", titleAlign = TextAlign.LEFT), "─ title ────", width = 12)
    }

    @Test
    @JsName("title_align_right")
    fun `title align right`() {
        checkRender(HorizontalRule("title", titleAlign = TextAlign.RIGHT), "──── title ─", width = 12)
    }

    @Test
    @JsName("multiline_title")
    fun `multiline title`() {
        checkRender(
            HorizontalRule(title = Text("Multiline\nHeader Text", whitespace = Whitespace.PRE_WRAP)),
            """
            ░     Multiline     ░
            ░─── Header Text ───░
            """,
            width = 19
        )
    }

    @Test
    @JsName("styled_title_and_rule")
    fun `styled title and rule`() {
        checkRender(
            HorizontalRule(title = blue("title"), ruleStyle = blue),
            blue("─── title ───"), width = 13
        )
    }

    @Test
    @JsName("themed_title_and_rule")
    fun `themed title and rule`() {
        checkRender(
            HorizontalRule(title = blue("title")),
            red("──  ${blue("title")}  ──"),
            width = 13,
            theme = Theme {
                styles["hr.rule"] = red
                dimensions["hr.title.padding"] = 2
            }
        )
    }
}
