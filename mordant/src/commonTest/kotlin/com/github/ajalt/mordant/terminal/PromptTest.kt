package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors.cyan
import com.github.ajalt.mordant.rendering.Whitespace
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

@OptIn(ExperimentalTerminalApi::class)
class PromptTest {
    private val vt = VirtualTerminalInterface(width = 8)
    private val t = Terminal(terminalInterface = vt)

    @Test
    fun stringPrompt() {
        vt.inputLines = mutableListOf("answer")
        StringPrompt("pr", t).ask() shouldBe "answer"
        vt.buffer() shouldBe "pr: "
    }

    @Test
    @JsName("StringPrompt_with_default")
    fun `StringPrompt with default`() {
        vt.inputLines = mutableListOf("")
        StringPrompt("pr", t, default = "def").ask() shouldBe "def"
        vt.buffer() shouldBe "pr: "
    }
    @Test
    @JsName("StringPrompt_with_choices")
    fun `StringPrompt with choices`() {
        vt.inputLines = mutableListOf("")
        StringPrompt("pr", t, choices = listOf("a", "b")).ask() shouldBe "def"
        vt.buffer() shouldBe "pr: "
    }
}
