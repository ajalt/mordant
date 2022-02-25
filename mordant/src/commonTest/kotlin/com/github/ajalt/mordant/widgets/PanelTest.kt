package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.rendering.Whitespace.PRE
import com.github.ajalt.mordant.test.RenderingTest
import kotlin.js.JsName
import kotlin.test.Test

class PanelTest : RenderingTest(width = 20) {

    @Test
    @JsName("no_expand")
    fun `no expand`() = checkRender(
        Panel(Text("text"), expand = false),
        """
        |╭────╮
        |│text│
        |╰────╯
        """
    )

    @Test
    fun expand() = checkRender(
        Panel(Text("text", align = TextAlign.CENTER), expand = true),
        """
        |╭──────────────────╮
        |│       text       │
        |╰──────────────────╯
        """
    )

    @Test
    @JsName("no_border")
    fun `no border`() = checkRender(
        Panel(Text("text\nline 2", whitespace = PRE), borderType = null),
        """
        |text  ⏎
        |line 2⏎
        """
    )

    @Test
    @JsName("default_title")
    fun `default title`() = checkRender(
        Panel("text content", title = "title"),
        """
        |╭── title ───╮
        |│text content│
        |╰────────────╯
        """
    )

    @Test
    @JsName("long_title")
    fun `long title`() = checkRender(
        Panel("content", title = "title title"),
        """
        |╭ title title ╮
        |│content      │
        |╰─────────────╯
        """
    )

    @Test
    @JsName("title_align_left")
    fun `title align left`() = checkRender(
        Panel("text content", title = "title", titleAlign = TextAlign.LEFT),
        """
        |╭─ title ────╮
        |│text content│
        |╰────────────╯
        """
    )

    @Test
    @JsName("title_align_right")
    fun `title align right`() = checkRender(
        Panel("text content", title = "title", titleAlign = TextAlign.RIGHT),
        """
        |╭──── title ─╮
        |│text content│
        |╰────────────╯
        """
    )

    @Test
    @JsName("bottom_title")
    fun `bottom title`() = checkRender(
        Panel("text content", bottomTitle = "title"),
        """
        |╭────────────╮
        |│text content│
        |╰── title ───╯
        """
    )

    @Test
    @JsName("title_widgets")
    fun `title widgets`() = checkRender(
        Panel(Text("text content"), title = Text("foo\nbar"), bottomTitle = Text("foo\nbar")),
        """
        |     foo      ⏎
        |╭─── bar ────╮
        |│text content│
        |╰─── foo ────╯
        |     bar      ⏎
        """
    )

    @Test
    @JsName("bottom_align")
    fun `bottom align`() = checkRender(
        Panel(
            "my panel content",
            title = "title",
            bottomTitle = "subtitle",
            titleAlign = TextAlign.LEFT,
            bottomTitleAlign = TextAlign.RIGHT
        ),
        """
        |╭─ title ────────╮
        |│my panel content│
        |╰───── subtitle ─╯
        """
    )

    @Test
    @JsName("themed_panel")
    fun `themed panel`() = checkRender(
        Panel(green("text content"), title = blue("title")),
        """
        |${red("╭───${blue("title")}────╮")}
        |${red("│${green("text content")}│")}
        |${red("╰────────────╯")}
        """,
        theme = Theme {
            styles["panel.border"] = red
            dimensions["panel.title.padding"] = 0
        },
    )
}
