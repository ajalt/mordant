package com.github.ajalt.mordant.terminal

import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

@OptIn(ExperimentalTerminalApi::class)
class PromptTest {
    private val vt = TerminalRecorder(width = 8)
    private val t = Terminal(terminalInterface = vt)

    @Test
    fun stringPrompt() {
        vt.inputLines = mutableListOf("answer")
        StringPrompt("pr", t).ask() shouldBe "answer"
        vt.output() shouldBe "pr: "
    }

    @Test
    @JsName("terminal_prompt")
    fun `terminal prompt`() {
        vt.inputLines = mutableListOf("answer")
        t.prompt("pr") shouldBe "answer"
        vt.output() shouldBe "pr: "
    }

    @Test
    @JsName("StringPrompt_with_default")
    fun `StringPrompt with default`() {
        vt.inputLines = mutableListOf("")
        // showDefault should be inferred, but is disabled due to KT-59326
        StringPrompt("pr", t, default = "def", showDefault = true).ask() shouldBe "def"
        val style = t.theme.style("prompt.default")
        vt.output() shouldBe "pr ${style("(def)")}: "
    }

    @Test
    @JsName("StringPrompt_with_choices")
    fun `StringPrompt with choices`() {
        vt.inputLines = mutableListOf("b")
        StringPrompt("pr", t, choices = listOf("a", "b")).ask() shouldBe "b"
        val s = t.theme.style("prompt.choices")
        vt.output() shouldBe "pr ${s("[a, b]")}: "
    }

    @Test
    @JsName("StringPrompt_invalid_choices")
    fun `StringPrompt invalid choice`() {
        vt.inputLines = mutableListOf("bad", "a")
        StringPrompt("pr", t, choices = listOf("a", "b"), ).ask() shouldBe "a"
        val s = t.theme.style("prompt.choices")
        val e = t.theme.danger
        val p = "pr ${s("[a, b]")}: "
        vt.output() shouldBe "$p${e("Invalid value, choose from [a, b]")}\n$p"
    }

    @Test
    @JsName("YesNoPrompt_no_default")
    fun `YesNoPrompt no default`() {
        vt.inputLines = mutableListOf("Y")
        YesNoPrompt("pr", t).ask() shouldBe true
        val style = t.theme.style("prompt.default")
        vt.output() shouldBe "pr ${style("[y/n]")}: "
    }

    @Test
    @JsName("YesNoPrompt_default")
    fun `YesNoPrompt default`() {
        vt.inputLines = mutableListOf("")
        YesNoPrompt("pr", t, default = false).ask() shouldBe false
        val style = t.theme.style("prompt.default")
        vt.output() shouldBe "pr ${style("[y/N]")}: "
    }
}
