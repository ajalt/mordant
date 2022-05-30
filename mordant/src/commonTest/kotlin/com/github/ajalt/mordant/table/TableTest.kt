package com.github.ajalt.mordant.table

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.TextAlign.CENTER
import com.github.ajalt.mordant.rendering.TextColors.blue
import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.rendering.VerticalAlign.MIDDLE
import com.github.ajalt.mordant.rendering.Whitespace.PRE
import com.github.ajalt.mordant.table.Borders.*
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.widgets.Padding
import com.github.ajalt.mordant.widgets.ProgressBar
import com.github.ajalt.mordant.widgets.Text
import kotlin.js.JsName
import kotlin.test.Test

class TableTest : RenderingTest() {
    @Test
    @JsName("empty_cell")
    fun `empty cell`() = doBodyTest("""
    ░┌┐
    ░││
    ░└┘
    """) {
        padding = Padding.none()
        row("")
    }

    @Test
    @JsName("border_top")
    fun `border top`() = doBodyTest("""
    ░───
    ░ 1 ░
    """) {
        row {
            cell(1) {
                cellBorders = TOP
            }
        }
    }

    @Test
    @JsName("border_right")
    fun `border right`() = doBodyTest("""
    ░ 1 │
    """) {
        row {
            cell(1) {
                cellBorders = RIGHT
            }
        }
    }

    @Test
    @JsName("border_bottom")
    fun `border bottom`() = doBodyTest("""
    ░ 1 ░
    ░───
    """) {
        row {
            cell(1) {
                cellBorders = BOTTOM
            }
        }
    }

    @Test
    @JsName("border_left")
    fun `border left`() = doBodyTest("""
    ░│ 1 ░
    """) {
        row {
            cell(1) {
                cellBorders = LEFT
            }
        }
    }

    @Test
    @JsName("border_top_with_corners")
    fun `border top with corners`() = doBodyTest("""
    ░╶───╴
    ░  1  ░
    ░┌───┐
    ░│ 2 │
    ░└───┘
    """) {
        row {
            cell(1) {
                cellBorders = TOP
            }
        }
        row(2)
    }

    @Test
    @JsName("border_right_with_corners")
    fun `border right with corners`() = doBodyTest("""
    ░┌───┐   ╷
    ░│ 1 │ 2 │
    ░└───┘   ╵
    """) {
        row {
            cell(1)
            cell(2) {
                cellBorders = RIGHT
            }
        }
    }

    @Test
    @JsName("border_bottom_with_corners")
    fun `border bottom with corners`() = doBodyTest("""
    ░┌───┐
    ░│ 1 │
    ░└───┘
    ░  2  ░
    ░╶───╴
    """) {
        row(1)
        row {
            cell(2) {
                cellBorders = BOTTOM
            }
        }
    }

    @Test
    @JsName("border_left_with_corners")
    fun `border left with corners`() = doBodyTest("""
    ░╷   ┌───┐
    ░│ 1 │ 2 │
    ░╵   └───┘
    """) {
        row {
            cell(1) {
                cellBorders = LEFT
            }
            cell(2)
        }
    }

    @Test
    @JsName("inside_borders")
    fun `inside borders`() = doBodyTest("""
    ░ 1 │ 2 ░
    ░───┼───░
    ░ 3 │ 4 ░
    """) {
        row {
            cell(1) {
                cellBorders = RIGHT
            }
            cell(2) {
                cellBorders = BOTTOM
            }
        }
        row {
            cell(3) {
                cellBorders = TOP
            }
            cell(4) {
                cellBorders = LEFT
            }
        }
    }


    @Test
    @JsName("empty_row")
    fun `empty row`() = doBodyTest("""
    ░┌───┐
    ░│ 1 │
    ░└───┘
    ░     ░
    ░┌───┐
    ░│ 2 │
    ░└───┘
    """) {
        row(1)
        row()
        row(2)
    }

    @Test
    @JsName("non_rectangular_table")
    fun `non-rectangular table`() = doBodyTest("""
    ░┌───┐       ░
    ░│ 1 │       ░
    ░├───┼───┐   ░
    ░│ 2 │ 3 │ 4 ░
    ░├───┼───┘   ░
    ░│ 5 │       ░
    ░└───┘       ░
    """) {
        row(1)
        row {
            cells(2, 3)
            cell(4) { cellBorders = NONE }
        }
        row(5)
    }

    @Test
    @JsName("preformatted_text_content")
    fun `preformatted text content`() = doBodyTest("""
    ░┌────────────────┬─┐
    ░│line 1          │2│
    ░│2nd line no wrap│ │
    ░├────────────────┼─┤
    ░│3               │4│
    ░└────────────────┴─┘
    """) {
        padding = Padding.none()
        row {
            cell(Text("""
                    line 1
                    2nd line no wrap
                    """.trimIndent(), whitespace = PRE)
            )
            cell(2)
        }
        row(3, 4)
    }

    @Test
    @JsName("wide_unicode_characters")
    fun `wide unicode characters`() = doBodyTest("""
    ░┌──────────┐
    ░│ 모ㄹ단ㅌ │
    ░├──────────┤
    ░│   媒人   │
    ░├──────────┤
    ░│  🙊🙉🙈  │
    ░├──────────┤
    ░│   1234   │
    ░└──────────┘
    """) {
        align = CENTER
        row("모ㄹ단ㅌ")
        row("媒人")
        row("🙊🙉🙈")
        row("1234")
    }

    @Test
    @JsName("striped_row_styles")
    fun `striped row styles`() = doBodyTest("""
    ░┌─────┐
    ░│${red("row 1")}│
    ░├─────┤
    ░│${blue("row 2")}│
    ░├─────┤
    ░│${red("row 3")}│
    ░├─────┤
    ░│${blue("row 4")}│
    ░└─────┘
    """) {
        rowStyles(TextStyle(red), TextStyle(blue))
        padding = Padding.none()
        row("row 1")
        row("row 2")
        row("row 3")
        row("row 4")
    }

    @Test
    @JsName("row_and_column_span_no_borders")
    fun `row and column span no borders`() = doBodyTest("""
    ░span1
    ░    2
    ░3 4 5
    """) {
        cellBorders = NONE
        padding = Padding.none()
        row {
            cell("span") {
                rowSpan = 2
                columnSpan = 2
            }
            cell(1)
        }
        row(2)
        row(3, 4, 5)
    }

    @Test
    @JsName("row_and_column_span")
    fun `row and column span`() = doBodyTest("""
    ░┌───────────┬───┐
    ░│           │ 1 │
    ░│           ├───┤
    ░│   span    │ 2 │
    ░│           ├───┤
    ░│           │ 3 │
    ░├───┬───┬───┼───┤
    ░│ 4 │ 5 │ 6 │ 7 │
    ░└───┴───┴───┴───┘
    """) {
        row {
            cell("span") {
                align = CENTER
                verticalAlign = MIDDLE
                rowSpan = 3
                columnSpan = 3
            }
            cell(1)
        }
        row(2)
        row(3)
        row(4, 5, 6, 7)
    }

    @Test
    @JsName("nested_tables")
    fun `nested tables`() = doBodyTest("""
    ░┌───────────┬───┐
    ░│ ┌───┬───┐ │ 5 │
    ░│ │ 1 │ 2 │ │   │
    ░│ ├───┼───┤ │   │
    ░│ │ 3 │ 4 │ │   │
    ░│ └───┴───┘ │   │
    ░├───────────┼───┤
    ░│ 6         │ 7 │
    ░└───────────┴───┘
    """) {
        row {
            cell(table {
                body {
                    row(1, 2)
                    row(3, 4)
                }

            })
            cell(5)
        }
        row(6, 7)
    }


    @Test
    @JsName("outer_border_none")
    fun `outer border none`() = doTest("""
    ░ 1  │ 2  │ 3  
    ░────┼────┼────
    ░ 4  │ 5  │ 6  
    ░ 7  │ 8  │ 9  
    ░────┼────┼────
    ░ 11 │ 12 │ 13 
    """) {
        tableBorders = NONE
        header { row(1, 2, 3) }
        body {
            cellBorders = LEFT_RIGHT
            row(4, 5, 6)
            row(7, 8, 9)
        }
        footer {
            row(11, 12, 13)
        }
    }

    @Test
    @JsName("border_outer_all_inner_left_right")
    fun `border outer all inner left right`() = doTest("""
    ░┌───┬───┬───┐
    ░│ 1 │ 2 │ 3 │
    ░├───┼───┼───┤
    ░│ 4 │ 5 │ 6 │
    ░│ 7 │ 8 │ 9 │
    ░└───┴───┴───┘
    """) {
        tableBorders = ALL
        header { row(1, 2, 3) }
        body {
            cellBorders = LEFT_RIGHT
            row(4, 5, 6)
            row(7, 8, 9)
        }
    }

    @Test
    @JsName("border_outer_all_inner_none")
    fun `border outer all inner none`() = doTest("""
    ░┌───────────┐
    ░│ 1   2   3 │
    ░│   ┌───┐   │
    ░│ 4 │ 5 │ 6 │
    ░│   └───┘   │
    ░│ 7   8   9 │
    ░└───────────┘
    """) {
        tableBorders = ALL
        body {
            cellBorders = NONE
            row(1, 2, 3)
            row {
                cell(4)
                cell(5){
                    cellBorders = ALL
                }
                cell(6)
            }
            row(7, 8, 9)
        }
    }

    @Test
    @JsName("section_column_builders")
    fun `section column builders`() = doTest("""
    ░┌─────┬─────┐
    ░│   1 │  2  │
    ░├─────┼─────┤
    ░│   3 │   4 │
    ░├─────┼─────┤
    ░│ 5   │  6  │
    ░├─────┼─────┤
    ░│ ... │ ... │
    ░└─────┴─────┘
    """) {
        column(0) {
            align = TextAlign.RIGHT
        }
        header {
            column(1) {
                align = CENTER
            }
            row(1, 2)
        }
        body {
            column(1) {
                align = TextAlign.RIGHT
            }
            row(3, 4)
        }
        footer {
            column(0) {
                align = TextAlign.LEFT
            }
            column(1) {
                align = CENTER
            }
            row(5, 6)
            row("...", "...")
        }
    }

    @Test
    fun captions() = doTest("""
    ░     top     ░
    ░┌───┬───┬───┐
    ░│ 1 │ 2 │ 3 │
    ░└───┴───┴───┘
    ░${blue("       bottom")}
    """) {
        captionTop("top")
        captionBottom(blue("bottom"), align = TextAlign.RIGHT)
        body { row(1, 2, 3) }
    }

    @Test
    @JsName("caption_widgets")
    fun `caption widgets`() = doTest("""
    ░!
    ░┌───┬───┬───┐
    ░│ 1 │ 2 │ 3 │
    ░└───┴───┴───┘
    ░!
    """) {
        val r = object : Widget {
            override fun measure(t: Terminal, width: Int) = WidthRange(1, 1)
            override fun render(t: Terminal, width: Int) = Lines(listOf(Line(listOf(Span.word("!")))))
        }
        captionTop(r)
        captionBottom(r)
        body { row(1, 2, 3) }
    }

    @Test
    fun grid() = checkRender(
        grid {
            column(0) { width = ColumnWidth.Fixed(3) }
            row(1, ".2.", 3)
            row(4, 5, 6)
        }, """
        ░1   .2. 3░
        ░4   5   6░
        """
    )

    @Test
    fun row() = checkRender(
        row {
            column(1) { width = ColumnWidth.Expand() }
            val bar = ProgressBar(indeterminate = true, showPulse = false, indeterminateStyle = TextStyle())
            cells(1, bar, 2)
        }, """
        ░1 ━━━━━━ 2░
        """, width = 10)

    @Test
    fun rowPadding() = checkRender(
        row(padding = 2) {
            column(1) { width = ColumnWidth.Expand() }
            val bar = ProgressBar(indeterminate = true, showPulse = false, indeterminateStyle = TextStyle())
            cells(1, bar, 2)
        }, """
        ░1  ━━━━  2░
        """, width = 10)

    @Test
    fun column() = checkRender(
        column {
            cell("1111")
            cells("2", "3") { align = TextAlign.RIGHT }
            cellsFrom(listOf(Text("4 4")))
        }, """
        ░1111░
        ░   2░
        ░   3░
        ░4 4 ░
        """)

    @Test
    fun columnPadding() = checkRender(
        column(padding = 1) {
            cell("1")
            cell("2")
            cell("3") { padding = Padding.of(top = 2) }
            cell("4") { padding = Padding.of(top = 0) }
            cell("5")

        }, """
        ░1░
        ░ ░
        ░2░
        ░ ░
        ░ ░
        ░3░
        ░4░
        ░ ░
        ░5░
        """)

    @Test
    @JsName("nesting_rows_in_columns")
    fun `nesting rows in columns`() = checkRender(
        column {
            cell(row { cells("1", "1") })
            cell(row { cells("222", "2") })

        }, """
        ░1 1  ░
        ░222 2░
        """)

    private fun doTest(expected: String, builder: TableBuilder.() -> Unit) {
        checkRender(table(builder), expected)
    }

    private fun doBodyTest(expected: String, builder: SectionBuilder.() -> Unit) {
        checkRender(table {
            borderType = BorderType.HEAVY_HEAD_FOOT
            body(builder)
        }, expected)
    }
}
