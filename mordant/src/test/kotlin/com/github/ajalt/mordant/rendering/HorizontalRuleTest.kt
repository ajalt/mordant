package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class HorizontalRuleTest {
    @Test
    fun `multiple character rules`() {
        Terminal(width = 20).render(
                HorizontalRule("1234", title = "title")
        ) shouldBe "123412 title 1234123"
    }

    @Test
    fun `multiline title`() {
        Terminal(width = 19).render(
                HorizontalRule(title = Text("""
                Multiline
                Header Text
                """.trimIndent()))
        ) shouldBe """
             Multiline     
        ─── Header Text ───
        """.trimIndent()
    }
}
