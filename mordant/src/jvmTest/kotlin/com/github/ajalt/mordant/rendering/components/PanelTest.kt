package com.github.ajalt.mordant.rendering.components

import com.github.ajalt.mordant.rendering.RenderingTest
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors.blue
import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.rendering.Whitespace.PRE
import com.github.ajalt.mordant.widgets.Panel
import com.github.ajalt.mordant.widgets.Text
import org.junit.Test

class PanelTest : RenderingTest(width = 20) {

    @Test
    fun `no expand`() = checkRender(
        Panel(Text("text"), expand = false),
        """
        ╭────╮
        │text│
        ╰────╯
        """
    )

    @Test
    fun expand() = checkRender(
        Panel(Text("text", align = TextAlign.CENTER), expand = true),
        """
        ╭──────────────────╮
        │       text       │
        ╰──────────────────╯
        """
    )

    @Test
    fun `no border`() = checkRender(
        Panel(Text("text\nline 2", whitespace = PRE), borderStyle = null),
        """
        |text  ⏎
        |line 2⏎
        """.trimMargin()
    )

    @Test
    fun `default title`() = checkRender(
        Panel("text content", title = "title"),
        """
        ╭── title ───╮
        │text content│
        ╰────────────╯
        """
    )

    @Test
    fun `long title`() = checkRender(
        Panel("content", title = "title title"),
        """
        ╭ title title ╮
        │content      │
        ╰─────────────╯
        """
    )

    @Test
    fun `title align left`() = checkRender(
        Panel("text content", title = "title", titleAlign = TextAlign.LEFT),
        """
        ╭─ title ────╮
        │text content│
        ╰────────────╯
        """
    )

    @Test
    fun `title align right`() = checkRender(
        Panel("text content", title = "title", titleAlign = TextAlign.RIGHT),
        """
        ╭──── title ─╮
        │text content│
        ╰────────────╯
        """
    )

    @Test
    fun `themed panel`() = checkRender(
        Panel("text content", title = "title"),
        """
        ${red("╭───${blue("title")}────╮")}
        ${red("│")}text content${red("│")}
        ${red("╰────────────╯")}
        """,
        theme = Theme {
            styles["panel.border"] = red
            styles["panel.title"] = blue
            dimensions["panel.title.padding"] = 0
        }
    )
}
