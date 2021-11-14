package com.github.ajalt.mordant.table

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.TextAlign.*
import com.github.ajalt.mordant.rendering.VerticalAlign.*
import com.github.ajalt.mordant.rendering.Whitespace.PRE
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.widgets.Text
import kotlin.js.JsName
import kotlin.test.Test

class TableAlignmentTest : RenderingTest() {
    @Test
    @JsName("top_none")
    fun `top none`() = doTextTest(TOP, NONE,
        """
            ┌─────────┬───┐
            │ 1 2     │ · │
            │         │ · │
            │         │ · │
            ├─────────┼───┤
            │ ······· │ · │
            └─────────┴───┘
            """
    )

    @Test
    @JsName("top_left")
    fun `top left`() = doTextTest(TOP, LEFT,
        """
            ┌─────────┬───┐
            │ 1 2     │ · │
            │         │ · │
            │         │ · │
            ├─────────┼───┤
            │ ······· │ · │
            └─────────┴───┘
            """
    )

    @Test
    @JsName("top_center")
    fun `top center`() = doTextTest(TOP, CENTER,
        """
            ┌─────────┬───┐
            │   1 2   │ · │
            │         │ · │
            │         │ · │
            ├─────────┼───┤
            │ ······· │ · │
            └─────────┴───┘
            """
    )

    @Test
    @JsName("top_justify")
    fun `top justify`() = doTextTest(TOP, JUSTIFY,
        """
            ┌─────────┬───┐
            │ 1     2 │ · │
            │         │ · │
            │         │ · │
            ├─────────┼───┤
            │ ······· │ · │
            └─────────┴───┘
            """
    )

    @Test
    @JsName("top_right")
    fun `top right`() = doTextTest(TOP, RIGHT,
        """
            ┌─────────┬───┐
            │     1 2 │ · │
            │         │ · │
            │         │ · │
            ├─────────┼───┤
            │ ······· │ · │
            └─────────┴───┘
            """
    )

    @Test
    @JsName("middle_none")
    fun `middle none`() = doTextTest(MIDDLE, NONE,
        """
            ┌─────────┬───┐
            │         │ · │
            │ 1 2     │ · │
            │         │ · │
            ├─────────┼───┤
            │ ······· │ · │
            └─────────┴───┘
            """
    )

    @Test
    @JsName("middle_left")
    fun `middle left`() = doTextTest(MIDDLE, LEFT,
        """
            ┌─────────┬───┐
            │         │ · │
            │ 1 2     │ · │
            │         │ · │
            ├─────────┼───┤
            │ ······· │ · │
            └─────────┴───┘
            """
    )

    @Test
    @JsName("middle_center")
    fun `middle center`() = doTextTest(MIDDLE, CENTER,
        """
            ┌─────────┬───┐
            │         │ · │
            │   1 2   │ · │
            │         │ · │
            ├─────────┼───┤
            │ ······· │ · │
            └─────────┴───┘
            """
    )

    @Test
    @JsName("middle_justify")
    fun `middle justify`() = doTextTest(MIDDLE, JUSTIFY,
        """
            ┌─────────┬───┐
            │         │ · │
            │ 1     2 │ · │
            │         │ · │
            ├─────────┼───┤
            │ ······· │ · │
            └─────────┴───┘
            """
    )

    @Test
    @JsName("middle_right")
    fun `middle right`() = doTextTest(MIDDLE, RIGHT,
        """
            ┌─────────┬───┐
            │         │ · │
            │     1 2 │ · │
            │         │ · │
            ├─────────┼───┤
            │ ······· │ · │
            └─────────┴───┘
            """
    )

    @Test
    @JsName("bottom_none")
    fun `bottom none`() = doTextTest(BOTTOM, NONE,
        """
            ┌─────────┬───┐
            │         │ · │
            │         │ · │
            │ 1 2     │ · │
            ├─────────┼───┤
            │ ······· │ · │
            └─────────┴───┘
            """
    )

    @Test
    @JsName("bottom_left")
    fun `bottom left`() = doTextTest(BOTTOM, LEFT,
        """
            ┌─────────┬───┐
            │         │ · │
            │         │ · │
            │ 1 2     │ · │
            ├─────────┼───┤
            │ ······· │ · │
            └─────────┴───┘
            """
    )

    @Test
    @JsName("bottom_center")
    fun `bottom center`() = doTextTest(BOTTOM, CENTER,
        """
            ┌─────────┬───┐
            │         │ · │
            │         │ · │
            │   1 2   │ · │
            ├─────────┼───┤
            │ ······· │ · │
            └─────────┴───┘
            """
    )

    @Test
    @JsName("bottom_justify")
    fun `bottom justify`() = doTextTest(BOTTOM, JUSTIFY,
        """
            ┌─────────┬───┐
            │         │ · │
            │         │ · │
            │ 1     2 │ · │
            ├─────────┼───┤
            │ ······· │ · │
            └─────────┴───┘
            """
    )

    @Test
    @JsName("bottom_right")
    fun `bottom right`() = doTextTest(BOTTOM, RIGHT,
        """
            ┌─────────┬───┐
            │         │ · │
            │         │ · │
            │     1 2 │ · │
            ├─────────┼───┤
            │ ······· │ · │
            └─────────┴───┘
            """
    )

    @Test
    @JsName("top_none_widget")
    fun `top none widget`() = doWidgetTest(TOP, NONE,
        """
            ┌──────┬───┐
            │ ×    │ · │
            │      │ · │
            │      │ · │
            ├──────┼───┤
            │ ···· │ · │
            └──────┴───┘
            """
    )

    @Test
    @JsName("top_left_widget")
    fun `top left widget`() = doWidgetTest(TOP, LEFT,
        """
            ┌──────┬───┐
            │ ×    │ · │
            │      │ · │
            │      │ · │
            ├──────┼───┤
            │ ···· │ · │
            └──────┴───┘
            """
    )

    @Test
    @JsName("top_center_widget")
    fun `top center widget`() = doWidgetTest(TOP, CENTER,
        """
            ┌──────┬───┐
            │  ×   │ · │
            │      │ · │
            │      │ · │
            ├──────┼───┤
            │ ···· │ · │
            └──────┴───┘
            """
    )

    @Test
    @JsName("top_justify_widget")
    fun `top justify widget`() = doWidgetTest(TOP, JUSTIFY,
        """
            ┌──────┬───┐
            │  ×   │ · │
            │      │ · │
            │      │ · │
            ├──────┼───┤
            │ ···· │ · │
            └──────┴───┘
            """
    )

    @Test
    @JsName("top_right_widget")
    fun `top right widget`() = doWidgetTest(TOP, RIGHT,
        """
            ┌──────┬───┐
            │    × │ · │
            │      │ · │
            │      │ · │
            ├──────┼───┤
            │ ···· │ · │
            └──────┴───┘
            """
    )

    @Test
    @JsName("middle_none_widget")
    fun `middle none widget`() = doWidgetTest(MIDDLE, NONE,
        """
            ┌──────┬───┐
            │      │ · │
            │ ×    │ · │
            │      │ · │
            ├──────┼───┤
            │ ···· │ · │
            └──────┴───┘
            """
    )

    @Test
    @JsName("middle_left_widget")
    fun `middle left widget`() = doWidgetTest(MIDDLE, LEFT,
        """
            ┌──────┬───┐
            │      │ · │
            │ ×    │ · │
            │      │ · │
            ├──────┼───┤
            │ ···· │ · │
            └──────┴───┘
            """
    )

    @Test
    @JsName("middle_center_widget")
    fun `middle center widget`() = doWidgetTest(MIDDLE, CENTER,
        """
            ┌──────┬───┐
            │      │ · │
            │  ×   │ · │
            │      │ · │
            ├──────┼───┤
            │ ···· │ · │
            └──────┴───┘
            """
    )

    @Test
    @JsName("middle_justify_widget")
    fun `middle justify widget`() = doWidgetTest(MIDDLE, JUSTIFY,
        """
            ┌──────┬───┐
            │      │ · │
            │  ×   │ · │
            │      │ · │
            ├──────┼───┤
            │ ···· │ · │
            └──────┴───┘
            """
    )

    @Test
    @JsName("middle_right_widget")
    fun `middle right widget`() = doWidgetTest(MIDDLE, RIGHT,
        """
            ┌──────┬───┐
            │      │ · │
            │    × │ · │
            │      │ · │
            ├──────┼───┤
            │ ···· │ · │
            └──────┴───┘
            """
    )

    @Test
    @JsName("bottom_none_widget")
    fun `bottom none widget`() = doWidgetTest(BOTTOM, NONE,
        """
            ┌──────┬───┐
            │      │ · │
            │      │ · │
            │ ×    │ · │
            ├──────┼───┤
            │ ···· │ · │
            └──────┴───┘
            """
    )

    @Test
    @JsName("bottom_left_widget")
    fun `bottom left widget`() = doWidgetTest(BOTTOM, LEFT,
        """
            ┌──────┬───┐
            │      │ · │
            │      │ · │
            │ ×    │ · │
            ├──────┼───┤
            │ ···· │ · │
            └──────┴───┘
            """
    )

    @Test
    @JsName("bottom_center_widget")
    fun `bottom center widget`() = doWidgetTest(BOTTOM, CENTER,
        """
            ┌──────┬───┐
            │      │ · │
            │      │ · │
            │  ×   │ · │
            ├──────┼───┤
            │ ···· │ · │
            └──────┴───┘
            """
    )

    @Test
    @JsName("bottom_justify_widget")
    fun `bottom justify widget`() = doWidgetTest(BOTTOM, JUSTIFY,
        """
            ┌──────┬───┐
            │      │ · │
            │      │ · │
            │  ×   │ · │
            ├──────┼───┤
            │ ···· │ · │
            └──────┴───┘
            """
    )

    @Test
    @JsName("bottom_right_widget")
    fun `bottom right widget`() = doWidgetTest(BOTTOM, RIGHT,
        """
            ┌──────┬───┐
            │      │ · │
            │      │ · │
            │    × │ · │
            ├──────┼───┤
            │ ···· │ · │
            └──────┴───┘
            """
    )


    private fun doTextTest(verticalAlign: VerticalAlign, align: TextAlign, expected: String) {
        checkRender(table {
            this.align = align
            this.verticalAlign = verticalAlign
            body {
                row {
                    cell("1 2")
                    cell(Text("·\n·\n·", whitespace = PRE))
                }
                rowFrom(listOf("·······", "·"))
            }
        }, expected)
    }

    private fun doWidgetTest(verticalAlign: VerticalAlign, align: TextAlign, expected: String) {
        checkRender(table {
            this.align = align
            this.verticalAlign = verticalAlign
            body {
                row {
                    cell(object : Widget {
                        override fun measure(t: Terminal, width: Int): WidthRange {
                            return WidthRange(1, 1)
                        }

                        override fun render(t: Terminal, width: Int): Lines {
                            return Lines(listOf(Line(listOf(Span.word("×")))))
                        }
                    })
                    cell(Text("·\n·\n·", whitespace = PRE))
                }
                row("····", "·")
            }
        }, expected)
    }
}
