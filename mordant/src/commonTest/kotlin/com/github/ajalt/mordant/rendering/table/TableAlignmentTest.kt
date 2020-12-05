package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.TextAlign.*
import com.github.ajalt.mordant.rendering.VerticalAlign.*
import com.github.ajalt.mordant.rendering.Whitespace.PRE
import com.github.ajalt.mordant.components.Text
import com.github.ajalt.mordant.table.table
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
    fun `top none renderable`() = doRenderableTest(TOP, NONE,
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
    fun `top left renderable`() = doRenderableTest(TOP, LEFT,
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
    fun `top center renderable`() = doRenderableTest(TOP, CENTER,
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
    fun `top justify renderable`() = doRenderableTest(TOP, JUSTIFY,
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
    fun `top right renderable`() = doRenderableTest(TOP, RIGHT,
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
    fun `middle none renderable`() = doRenderableTest(MIDDLE, NONE,
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
    fun `middle left renderable`() = doRenderableTest(MIDDLE, LEFT,
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
    fun `middle center renderable`() = doRenderableTest(MIDDLE, CENTER,
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
    fun `middle justify renderable`() = doRenderableTest(MIDDLE, JUSTIFY,
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
    fun `middle right renderable`() = doRenderableTest(MIDDLE, RIGHT,
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
    fun `bottom none renderable`() = doRenderableTest(BOTTOM, NONE,
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
    fun `bottom left renderable`() = doRenderableTest(BOTTOM, LEFT,
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
    fun `bottom center renderable`() = doRenderableTest(BOTTOM, CENTER,
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
    fun `bottom justify renderable`() = doRenderableTest(BOTTOM, JUSTIFY,
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
    fun `bottom right renderable`() = doRenderableTest(BOTTOM, RIGHT,
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

    private fun doRenderableTest(verticalAlign: VerticalAlign, align: TextAlign, expected: String) {
        checkRender(table {
            this.align = align
            this.verticalAlign = verticalAlign
            body {
                row {
                    cell(object : Renderable {
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
