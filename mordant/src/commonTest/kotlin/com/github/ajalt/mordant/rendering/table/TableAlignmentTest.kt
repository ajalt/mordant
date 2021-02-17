package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.TextAlign.*
import com.github.ajalt.mordant.rendering.VerticalAlign.*
import com.github.ajalt.mordant.rendering.Whitespace.PRE
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.widgets.Text
import kotlin.test.Test

class TableAlignmentTest : RenderingTest() {
    @Test
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
                            return Lines(listOf(listOf(Span.word("×"))))
                        }
                    })
                    cell(Text("·\n·\n·", whitespace = PRE))
                }
                row("····", "·")
            }
        }, expected)
    }
}
