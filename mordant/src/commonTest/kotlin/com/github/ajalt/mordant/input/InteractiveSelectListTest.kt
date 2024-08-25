package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.widgets.SelectList.Entry
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.js.JsName
import kotlin.test.Test

class InteractiveSelectListTest {
    private val rec = TerminalRecorder(
        width = 24, ansiLevel = AnsiLevel.NONE, inputInteractive = true, outputInteractive = true
    )
    private val t = Terminal(terminalInterface = rec)

    @[Test JsName("single_select_strings")]
    fun `single select strings`() = doSingleSelectTest {
        t.interactiveSelectList(listOf("a", "b", "c"))
    }

    @[Test JsName("single_select_entries")]
    fun `single select entries`() = doSingleSelectTest {
        t.interactiveSelectList(listOf(Entry("a"), Entry("b"), Entry("c")))
    }

    @[Test JsName("multi_select_strings")]
    fun `multi select strings`() = doMultiSelectTest {
        t.interactiveMultiSelectList(listOf("a", "b", "c"))
    }

    @[Test JsName("multi_select_entries")]
    fun `multi select entries`() = doMultiSelectTest {
        t.interactiveMultiSelectList(listOf(Entry("a"), Entry("b"), Entry("c")))
    }

    private fun doSingleSelectTest(runList: () -> String?) {
        rec.inputEvents = mutableListOf(KeyboardEvent("ArrowDown"), KeyboardEvent("Enter"))
        runList() shouldBe "b"
        rec.stdout() shouldContain  """
        ░❯ a
        ░  b
        ░  c
        """.trimMargin("░")
    }

    private fun doMultiSelectTest(runList: () -> List<String>?) {
        rec.inputEvents = mutableListOf(
            KeyboardEvent("ArrowDown"),
            KeyboardEvent("x"),
            KeyboardEvent("ArrowDown"),
            KeyboardEvent("x"),
            KeyboardEvent("Enter"),
        )
        runList() shouldBe listOf("b", "c")
        rec.stdout() shouldContain  """
        ░❯ • a
        ░  • b
        ░  • c
        """.trimMargin("░")
    }
}
