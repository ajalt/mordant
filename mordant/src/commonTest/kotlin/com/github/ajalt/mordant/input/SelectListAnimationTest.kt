package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.test.latestOutput
import com.github.ajalt.mordant.test.shouldMatchRender
import com.github.ajalt.mordant.widgets.SelectList
import com.github.ajalt.mordant.widgets.Text
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SelectListAnimationTest {
    private val rec = TerminalRecorder(
        width = 24, ansiLevel = AnsiLevel.NONE, inputInteractive = true, outputInteractive = true
    )
    private val t = Terminal(terminalInterface = rec)
    private val b = InteractiveSelectListBuilder(t).showInstructions(false)
    private val down = KeyboardEvent("ArrowDown")
    private val up = KeyboardEvent("ArrowUp")
    private val slash = KeyboardEvent("/")
    private val enter = KeyboardEvent("Enter")
    private val esc = KeyboardEvent("Escape")
    private val x = KeyboardEvent("x")

    @[Test JsName("multi_select_minimum")]
    fun `multi select minimum`() {
        val a = b.minimumSelections(2).limit(2)
            .addEntry("a", selected = true).addEntry("b").createMultiSelectInputAnimation()
        a.receiveEvent(enter) shouldBe InputReceiver.Status.Continue
        a.receiveEvent(down)
        a.receiveEvent(x)
        a.receiveEvent(enter) shouldBe InputReceiver.Status.Finished(listOf("a", "b"))
    }

    @[Test JsName("minimum_counts_selections_hidden_by_filter")]
    fun `minimum counts selections hidden by filter`() {
        val a = b.entries("a", "b").minimumSelections(2).filterable(true)
            .createMultiSelectInputAnimation()
        a.receiveEvent(x)
        a.receiveEvent(slash)
        a.receiveEvent(KeyboardEvent("b"))
        a.receiveEvent(enter) shouldBe InputReceiver.Status.Continue
        a.receiveEvent(enter) shouldBe InputReceiver.Status.Continue
        a.receiveEvent(x)
        a.receiveEvent(enter) shouldBe InputReceiver.Status.Finished(listOf("a", "b"))
    }

    @[Test JsName("reusing_builder_preserves_each_lists_bounds")]
    fun `reusing builder preserves each lists bounds`() {
        val first = b.entries("a", "b").minimumSelections(2).limit(2)
            .createMultiSelectInputAnimation()
        val second = b.entries("c").minimumSelections(1).limit(1)
            .createMultiSelectInputAnimation()
        first.receiveEvent(x)
        first.receiveEvent(enter) shouldBe InputReceiver.Status.Continue
        first.receiveEvent(down)
        first.receiveEvent(x)
        first.receiveEvent(enter) shouldBe InputReceiver.Status.Finished(listOf("a", "b"))
        b.minimumSelections(2).limit(2).entries("d", "e")
        second.receiveEvent(x)
        second.receiveEvent(enter) shouldBe InputReceiver.Status.Finished(listOf("c"))
    }

    @[Test JsName("multi_select_defaults_to_no_minimum")]
    fun `multi select defaults to no minimum`() {
        val a = b.entries("a").createMultiSelectInputAnimation()
        a.receiveEvent(enter) shouldBe InputReceiver.Status.Finished(emptyList<String>())
    }

    @[Test JsName("minimum_allows_cancellation_and_ignores_single_select")]
    fun `minimum allows cancellation and ignores single select`() {
        b.entries("a", "b").minimumSelections(2)
        b.createMultiSelectInputAnimation().receiveEvent(KeyboardEvent("c", ctrl = true)) shouldBe
                InputReceiver.Status.Finished(null)
        b.createSingleSelectInputAnimation().receiveEvent(enter) shouldBe
                InputReceiver.Status.Finished("a")
    }

    @[Test JsName("unreachable_multi_select_minimum")]
    fun `unreachable multi select minimum`() {
        assertFailsWith<IllegalArgumentException> { b.minimumSelections(-1) }
        b.entries("a", "b").minimumSelections(3)
        assertFailsWith<IllegalArgumentException> { b.createMultiSelectInputAnimation() }
        b.minimumSelections(2).limit(1)
        assertFailsWith<IllegalArgumentException> { b.createMultiSelectInputAnimation() }
    }

    @[Test JsName("single_select_instructions")]
    fun `single select instructions`() {
        val a = b.entries("a", "b")
            .title("title")
            .showInstructions(true)
            .filterable(true)
            .createSingleSelectInputAnimation()

        rec.latestOutput() shouldMatchRender """
        ░title
        ░❯ a
        ░  b
        ░↑ up • ↓ down • / filter • enter select
        """

        a.receiveEvent(slash)
        rec.latestOutput() shouldMatchRender """
        ░/ ░
        ░❯ a
        ░  b
        ░↑ up • ↓ down • esc clear filter • enter select
        """

        a.receiveEvent(esc)
        rec.latestOutput() shouldMatchRender """
        ░title
        ░❯ a
        ░  b
        ░↑ up • ↓ down • / filter • enter select
        """

        a.clear()
        b.filterable(false)
            .createSingleSelectInputAnimation()
        rec.latestOutput() shouldMatchRender """
        ░title
        ░❯ a
        ░  b
        ░↑ up • ↓ down • enter select
        """
    }


    @[Test JsName("multi_select_instructions")]
    fun `multi select instructions`() {
        val a = b.entries("a", "b")
            .title("title")
            .showInstructions(true)
            .filterable(true)
            .createMultiSelectInputAnimation()

        rec.latestOutput() shouldMatchRender """
        ░title
        ░❯ • a
        ░  • b
        ░x toggle • ↑ up • ↓ down • / filter • enter confirm
        """

        a.receiveEvent(slash)
        rec.latestOutput() shouldMatchRender """
        ░/ ░
        ░  • a
        ░  • b
        ░esc clear filter • enter set filter
        """

        a.receiveEvent(esc)
        rec.latestOutput() shouldMatchRender """
        ░title
        ░❯ • a
        ░  • b
        ░x toggle • ↑ up • ↓ down • / filter • enter confirm
        """

        a.receiveEvent(slash)
        a.receiveEvent(x)
        a.receiveEvent(enter)
        rec.latestOutput() shouldMatchRender """
        ░title
        ░
        ░x toggle • ↑ up • ↓ down • / filter • esc clear filter • enter confirm
        """

        a.clear()
        b.filterable(false)
            .createMultiSelectInputAnimation()
        rec.latestOutput() shouldMatchRender """
        ░title
        ░❯ • a
        ░  • b
        ░x toggle • ↑ up • ↓ down • enter confirm
        """
    }

    @[Test JsName("cursor_movement")]
    fun `cursor movement`() {
        val a = b.entries("a", "b", "c")
            .createSingleSelectInputAnimation()

        a.receiveEvent(down) shouldBe InputReceiver.Status.Continue
        rec.latestOutput() shouldMatchRender """
        ░  a
        ░❯ b
        ░  c
        """

        a.receiveEvent(down) shouldBe InputReceiver.Status.Continue
        rec.latestOutput() shouldMatchRender """
        ░  a
        ░  b
        ░❯ c
        """

        a.receiveEvent(down)
        rec.latestOutput() shouldMatchRender """
        ░  a
        ░  b
        ░❯ c
        """

        a.receiveEvent(up)
        rec.latestOutput() shouldMatchRender """
        ░  a
        ░❯ b
        ░  c
        """

        a.receiveEvent(up)
        rec.latestOutput() shouldMatchRender """
        ░❯ a
        ░  b
        ░  c
        """

        a.receiveEvent(up)
        rec.latestOutput() shouldMatchRender """
        ░❯ a
        ░  b
        ░  c
        """
    }

    @[Test JsName("filtered_cursor_movement")]
    fun `filtered cursor movement`() {
        val a = b.entries("1", "ax", "2", "bx", "3", "cx", "4")
            .filterable(true)
            .createSingleSelectInputAnimation()

        a.receiveEvent(slash)
        a.receiveEvent(x)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░❯ ax
        ░  bx
        ░  cx
        """

        a.receiveEvent(down)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░  ax
        ░❯ bx
        ░  cx
        """

        a.receiveEvent(down)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░  ax
        ░  bx
        ░❯ cx
        """

        a.receiveEvent(down)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░  ax
        ░  bx
        ░❯ cx
        """

        a.receiveEvent(up)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░  ax
        ░❯ bx
        ░  cx
        """

        a.receiveEvent(up)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░❯ ax
        ░  bx
        ░  cx
        """

        a.receiveEvent(up)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░❯ ax
        ░  bx
        ░  cx
        """

        a.receiveEvent(down)
        a.receiveEvent(enter) shouldBe InputReceiver.Status.Finished("bx")
    }

    @[Test JsName("filtered_to_empty")]
    fun `filtered to empty`() {
        val a = b.entries("a")
            .filterable(true)
            .createSingleSelectInputAnimation()
        a.receiveEvent(slash)
        a.receiveEvent(x)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░
        """

        a.receiveEvent(down)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░
        """
    }

    @[Test JsName("always_show_descriptions")]
    fun `always show descriptions`() {
        b
            .addEntry("a", "adesc")
            .addEntry("b", Text("bdesc"))
            .createSingleSelectInputAnimation()

        rec.latestOutput() shouldMatchRender """
        ░❯ a    ░
        ░  adesc░
        ░  b    ░
        ░  bdesc░
        """
    }

    @[Test JsName("only_show_active_description")]
    fun `only show active description`() {
        val a = b
            .addEntry("ax", "adesc")
            .addEntry("b", Text("bdesc"))
            .addEntry(SelectList.Entry("cx", "cdesc"))
            .onlyShowActiveDescription(true)
            .filterable(true)
            .createSingleSelectInputAnimation()

        rec.latestOutput() shouldMatchRender """
        ░❯ ax   ░
        ░  adesc░
        ░  b    ░
        ░  cx   ░
        """

        a.receiveEvent(down)
        rec.latestOutput() shouldMatchRender """
        ░  ax   ░
        ░❯ b    ░
        ░  bdesc░
        ░  cx   ░
        """

        a.receiveEvent(down)
        rec.latestOutput() shouldMatchRender """
        ░  ax   ░
        ░  b    ░
        ░❯ cx   ░
        ░  cdesc░
        """

        a.receiveEvent(slash)
        a.receiveEvent(x)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░  ax   ░
        ░❯ cx   ░
        ░  cdesc░
        """

        a.receiveEvent(up)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░❯ ax   ░
        ░  adesc░
        ░  cx   ░
        """
    }

    @[Test JsName("filtering_multi_select")]
    fun `filtering multi select`() {
        val a = b.entries("ax", "b", "cx")
            .filterable(true)
            .createMultiSelectInputAnimation()

        rec.latestOutput() shouldMatchRender """
        ░❯ • ax
        ░  • b 
        ░  • cx
        """

        a.receiveEvent(x)
        rec.latestOutput() shouldMatchRender """
        ░❯ ✓ ax
        ░  • b 
        ░  • cx
        """

        a.receiveEvent(slash)
        a.receiveEvent(x)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░  ✓ ax
        ░  • cx
        """

        a.receiveEvent(enter)
        rec.latestOutput() shouldMatchRender """
        ░❯ ✓ ax
        ░  • cx
        """

        a.receiveEvent(down)
        a.receiveEvent(x)
        rec.latestOutput() shouldMatchRender """
        ░  ✓ ax
        ░❯ ✓ cx
        """

        a.receiveEvent(esc)
        rec.latestOutput() shouldMatchRender """
        ░  ✓ ax
        ░  • b 
        ░❯ ✓ cx
        """

        a.receiveEvent(enter) shouldBe InputReceiver.Status.Finished(listOf("ax", "cx"))
    }

}
