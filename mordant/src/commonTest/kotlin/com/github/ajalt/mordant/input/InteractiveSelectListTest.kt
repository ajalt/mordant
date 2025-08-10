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
    fun `single select strings`() = doSingleSelectTest({
        t.interactiveSelectList(listOf("a", "b", "c"))
    }, "b")

    @[Test JsName("single_select_entries")]
    fun `single select entries`() = doSingleSelectTest({
        t.interactiveSelectList(listOf(Entry("a"), Entry("b"), Entry("c")))
    }, "b")

    @[Test JsName("single_select_entries_with_value")]
    fun `single select entries with value`() = doSingleSelectTest({
        t.interactiveSelectList(listOf(Entry("a", value = "AA"), Entry("b", value = "BB"), Entry("c", value = "CC")))
    }, "BB")

    @[Test JsName("multi_select_strings")]
    fun `multi select strings`() = doMultiSelectTest({
        t.interactiveMultiSelectList(listOf("a", "b", "c"))
    }, listOf("b", "c"))

    @[Test JsName("multi_select_entries")]
    fun `multi select entries`() = doMultiSelectTest({
        t.interactiveMultiSelectList(listOf(Entry("a"), Entry("b"), Entry("c")))
    }, listOf("b", "c"))

    @[Test JsName("multi_select_entries_with_values")]
    fun `multi select entries with values`() = doMultiSelectTest({
        t.interactiveMultiSelectList(listOf(Entry("a", value = "AA"), Entry("b", value = "BB"), Entry("c", value = "CC")))
    }, listOf("BB", "CC"))

    private fun doSingleSelectTest(runList: () -> String?, expected: String) {
        rec.inputEvents = mutableListOf(KeyboardEvent("ArrowDown"), KeyboardEvent("Enter"))
        runList() shouldBe expected
        rec.stdout() shouldContain  """
        ░❯ a
        ░  b
        ░  c
        """.trimMargin("░")
    }

    private fun doMultiSelectTest(runList: () -> List<String>?, expected: List<String>) {
        rec.inputEvents = mutableListOf(
            KeyboardEvent("ArrowDown"),
            KeyboardEvent("x"),
            KeyboardEvent("ArrowDown"),
            KeyboardEvent("x"),
            KeyboardEvent("Enter"),
        )
        runList() shouldBe expected
        rec.stdout() shouldContain  """
        ░❯ • a
        ░  • b
        ░  • c
        """.trimMargin("░")
    }
}
