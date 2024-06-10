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

    @Test
    @JsName("single_select_instructions")
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

        a.onEvent(slash)
        rec.latestOutput() shouldMatchRender """
        ░/ ░
        ░❯ a
        ░  b
        ░↑ up • ↓ down • esc clear filter • enter select
        """

        a.onEvent(esc)
        rec.latestOutput() shouldMatchRender """
        ░title
        ░❯ a
        ░  b
        ░↑ up • ↓ down • / filter • enter select
        """

        a.cancel()
        b.filterable(false)
            .createSingleSelectInputAnimation()
        rec.latestOutput() shouldMatchRender """
        ░title
        ░❯ a
        ░  b
        ░↑ up • ↓ down • enter select
        """
    }


    @Test
    @JsName("multi_select_instructions")
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

        a.onEvent(slash)
        rec.latestOutput() shouldMatchRender """
        ░/ ░
        ░  • a
        ░  • b
        ░esc clear filter • enter set filter
        """

        a.onEvent(esc)
        rec.latestOutput() shouldMatchRender """
        ░title
        ░❯ • a
        ░  • b
        ░x toggle • ↑ up • ↓ down • / filter • enter confirm
        """

        a.onEvent(slash)
        a.onEvent(x)
        a.onEvent(enter)
        rec.latestOutput() shouldMatchRender """
        ░title
        ░
        ░x toggle • ↑ up • ↓ down • / filter • esc clear filter • enter confirm
        """

        a.cancel()
        b.filterable(false)
            .createMultiSelectInputAnimation()
        rec.latestOutput() shouldMatchRender """
        ░title
        ░❯ • a
        ░  • b
        ░x toggle • ↑ up • ↓ down • enter confirm
        """
    }

    @Test
    @JsName("cursor_movement")
    fun `cursor movement`() {
        val a = b.entries("a", "b", "c")
            .createSingleSelectInputAnimation()

        a.onEvent(down) shouldBe InputReceiver.Status.Continue
        rec.latestOutput() shouldMatchRender """
        ░  a
        ░❯ b
        ░  c
        """

        a.onEvent(down) shouldBe InputReceiver.Status.Continue
        rec.latestOutput() shouldMatchRender """
        ░  a
        ░  b
        ░❯ c
        """

        a.onEvent(down)
        rec.latestOutput() shouldMatchRender """
        ░  a
        ░  b
        ░❯ c
        """

        a.onEvent(up)
        rec.latestOutput() shouldMatchRender """
        ░  a
        ░❯ b
        ░  c
        """

        a.onEvent(up)
        rec.latestOutput() shouldMatchRender """
        ░❯ a
        ░  b
        ░  c
        """

        a.onEvent(up)
        rec.latestOutput() shouldMatchRender """
        ░❯ a
        ░  b
        ░  c
        """
    }

    @Test
    @JsName("filtered_cursor_movement")
    fun `filtered cursor movement`() {
        val a = b.entries("1", "ax", "2", "bx", "3", "cx", "4")
            .filterable(true)
            .createSingleSelectInputAnimation()

        a.onEvent(slash)
        a.onEvent(x)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░❯ ax
        ░  bx
        ░  cx
        """

        a.onEvent(down)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░  ax
        ░❯ bx
        ░  cx
        """

        a.onEvent(down)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░  ax
        ░  bx
        ░❯ cx
        """

        a.onEvent(down)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░  ax
        ░  bx
        ░❯ cx
        """

        a.onEvent(up)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░  ax
        ░❯ bx
        ░  cx
        """

        a.onEvent(up)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░❯ ax
        ░  bx
        ░  cx
        """

        a.onEvent(up)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░❯ ax
        ░  bx
        ░  cx
        """

        a.onEvent(down)
        a.onEvent(enter) shouldBe InputReceiver.Status.Finished("bx")
    }

    @Test
    @JsName("filtered_to_empty")
    fun `filtered to empty`() {
        val a = b.entries("a")
            .filterable(true)
            .createSingleSelectInputAnimation()
        a.onEvent(slash)
        a.onEvent(x)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░
        """

        a.onEvent(down)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░
        """
    }

    @Test
    @JsName("always_show_descriptions")
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

    @Test
    @JsName("only_show_active_description")
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

        a.onEvent(down)
        rec.latestOutput() shouldMatchRender """
        ░  ax   ░
        ░❯ b    ░
        ░  bdesc░
        ░  cx   ░
        """

        a.onEvent(down)
        rec.latestOutput() shouldMatchRender """
        ░  ax   ░
        ░  b    ░
        ░❯ cx   ░
        ░  cdesc░
        """

        a.onEvent(slash)
        a.onEvent(x)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░  ax   ░
        ░❯ cx   ░
        ░  cdesc░
        """

        a.onEvent(up)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░❯ ax   ░
        ░  adesc░
        ░  cx   ░
        """
    }

    @Test
    @JsName("filtering_multi_select")
    fun `filtering multi select`() {
        val a = b.entries("ax", "b", "cx")
            .filterable(true)
            .createMultiSelectInputAnimation()

        rec.latestOutput() shouldMatchRender """
        ░❯ • ax
        ░  • b 
        ░  • cx
        """

        a.onEvent(x)
        rec.latestOutput() shouldMatchRender """
        ░❯ ✓ ax
        ░  • b 
        ░  • cx
        """

        a.onEvent(slash)
        a.onEvent(x)
        rec.latestOutput() shouldMatchRender """
        ░/ x
        ░  ✓ ax
        ░  • cx
        """

        a.onEvent(enter)
        rec.latestOutput() shouldMatchRender """
        ░❯ ✓ ax
        ░  • cx
        """

        a.onEvent(down)
        a.onEvent(x)
        rec.latestOutput() shouldMatchRender """
        ░  ✓ ax
        ░❯ ✓ cx
        """

        a.onEvent(esc)
        rec.latestOutput() shouldMatchRender """
        ░  ✓ ax
        ░  • b 
        ░❯ ✓ cx
        """

        a.onEvent(enter) shouldBe InputReceiver.Status.Finished(listOf("ax", "cx"))
    }

}
