package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.terminal.TextColors.blue
import com.github.ajalt.mordant.terminal.TextColors.red
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.TextAlign.CENTER
import com.github.ajalt.mordant.rendering.VerticalAlign.MIDDLE
import com.github.ajalt.mordant.rendering.Whitespace.PRE
import com.github.ajalt.mordant.components.Text
import com.github.ajalt.mordant.table.Borders.*
import com.github.ajalt.mordant.table.SectionBuilder
import com.github.ajalt.mordant.table.TableBuilder
import com.github.ajalt.mordant.table.table
import kotlin.test.Test

class TableTest : RenderingTest() {
    @Test
    fun `empty cell`() = doBodyTest("""
    |┌┐
    |││
    |└┘
    """) {
        padding = com.github.ajalt.mordant.components.Padding.none()
        row("")
    }

    @Test
    fun `border top`() = doBodyTest("""
    |───
    | 1 ⏎
    """) {
        row {
            cell(1) {
                borders = TOP
            }
        }
    }

    @Test
    fun `border right`() = doBodyTest("""
    | 1 │
    """) {
        row {
            cell(1) {
                borders = RIGHT
            }
        }
    }

    @Test
    fun `border bottom`() = doBodyTest("""
    | 1 ⏎
    |───
    """) {
        row {
            cell(1) {
                borders = BOTTOM
            }
        }
    }

    @Test
    fun `border left`() = doBodyTest("""
    |│ 1 ⏎
    """) {
        row {
            cell(1) {
                borders = LEFT
            }
        }
    }

    @Test
    fun `border top with corners`() = doBodyTest("""
    |╶───╴
    |  1  ⏎
    |┌───┐
    |│ 2 │
    |└───┘
    """) {
        row {
            cell(1) {
                borders = TOP
            }
        }
        row(2)
    }

    @Test
    fun `border right with corners`() = doBodyTest("""
    |┌───┐   ╷
    |│ 1 │ 2 │
    |└───┘   ╵
    """) {
        row {
            cell(1)
            cell(2) {
                borders = RIGHT
            }
        }
    }

    @Test
    fun `border bottom with corners`() = doBodyTest("""
    |┌───┐
    |│ 1 │
    |└───┘
    |  2  ⏎
    |╶───╴
    """) {
        row(1)
        row {
            cell(2) {
                borders = BOTTOM
            }
        }
    }

    @Test
    fun `border left with corners`() = doBodyTest("""
    |╷   ┌───┐
    |│ 1 │ 2 │
    |╵   └───┘
    """) {
        row {
            cell(1) {
                borders = LEFT
            }
            cell(2)
        }
    }

    @Test
    fun `inside borders`() = doBodyTest("""
    | 1 │ 2 ⏎
    |───┼───⏎
    | 3 │ 4 ⏎
    """) {
        row {
            cell(1) {
                borders = RIGHT
            }
            cell(2) {
                borders = BOTTOM
            }
        }
        row {
            cell(3) {
                borders = TOP
            }
            cell(4) {
                borders = LEFT
            }
        }
    }


    @Test
    fun `empty row`() = doBodyTest("""
    |┌───┐
    |│ 1 │
    |└───┘
    |     ⏎
    |┌───┐
    |│ 2 │
    |└───┘
    """) {
        row(1)
        row()
        row(2)
    }

    @Test
    fun `non-rectangular table`() = doBodyTest("""
    |┌───┐       ⏎
    |│ 1 │       ⏎
    |├───┼───┐   ⏎
    |│ 2 │ 3 │ 4 ⏎
    |├───┼───┘   ⏎
    |│ 5 │       ⏎
    |└───┘       ⏎
    """) {
        row(1)
        row {
            cells(2, 3)
            cell(4) { borders = NONE }
        }
        row(5)
    }

    @Test
    fun `preformatted text content`() = doBodyTest("""
    |┌────────────────┬─┐
    |│line 1          │2│
    |│2nd line no wrap│ │
    |├────────────────┼─┤
    |│3               │4│
    |└────────────────┴─┘
    """) {
        padding = com.github.ajalt.mordant.components.Padding.none()
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
    fun `wide unicode characters`() = doBodyTest("""
    |┌──────────┐
    |│ 모ㄹ단ㅌ │
    |├──────────┤
    |│   媒人   │
    |├──────────┤
    |│  🙊🙉🙈  │
    |├──────────┤
    |│   1234   │
    |└──────────┘
    """) {
        align = CENTER
        row("모ㄹ단ㅌ")
        row("媒人")
        row("🙊🙉🙈")
        row("1234")
    }

    @Test
    fun `striped row styles`() = doBodyTest("""
    |┌─────┐
    |│${red("row 1")}│
    |├─────┤
    |│${blue("row 2")}│
    |├─────┤
    |│${red("row 3")}│
    |├─────┤
    |│${blue("row 4")}│
    |└─────┘
    """) {
        rowStyles(TextStyle(red), TextStyle(blue))
        padding = com.github.ajalt.mordant.components.Padding.none()
        row("row 1")
        row("row 2")
        row("row 3")
        row("row 4")
    }

    @Test
    fun `row and column span no borders`() = doBodyTest("""
    |span1
    |    2
    |3 4 5
    """) {
        borders = NONE
        padding = com.github.ajalt.mordant.components.Padding.none()
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
    fun `row and column span`() = doBodyTest("""
    |┌───────────┬───┐
    |│           │ 1 │
    |│           ├───┤
    |│   span    │ 2 │
    |│           ├───┤
    |│           │ 3 │
    |├───┬───┬───┼───┤
    |│ 4 │ 5 │ 6 │ 7 │
    |└───┴───┴───┴───┘
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
    fun `nested tables`() = doBodyTest("""
    |┌───────────┬───┐
    |│ ┌───┬───┐ │ 5 │
    |│ │ 1 │ 2 │ │   │
    |│ ├───┼───┤ │   │
    |│ │ 3 │ 4 │ │   │
    |│ └───┴───┘ │   │
    |├───────────┼───┤
    |│ 6         │ 7 │
    |└───────────┴───┘
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
    fun `outer border`() = doTest("""
    | 1  │ 2  │ 3  
    |────┼────┼────
    | 4  │ 5  │ 6  
    | 7  │ 8  │ 9  
    |────┼────┼────
    | 11 │ 12 │ 13 
    """) {
        outerBorder = false
        header { row(1, 2, 3) }
        body {
            borders = LEFT_RIGHT
            row(4, 5, 6)
            row(7, 8, 9)
        }
        footer {
            row(11, 12, 13)
        }
    }

    @Test
    fun `section column builders`() = doTest("""
    |┌─────┬─────┐
    |│   1 │  2  │
    |├─────┼─────┤
    |│   3 │   4 │
    |├─────┼─────┤
    |│ 5   │  6  │
    |├─────┼─────┤
    |│ ... │ ... │
    |└─────┴─────┘
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
    |     top     ⏎
    |┌───┬───┬───┐
    |│ 1 │ 2 │ 3 │
    |└───┴───┴───┘
    |${blue("       bottom")}
    """) {
        captionTop("top")
        captionBottom("bottom", align = TextAlign.RIGHT, style = TextStyle(blue))
        body { row(1, 2, 3) }
    }

    @Test
    fun grid() = checkRender(com.github.ajalt.mordant.table.grid {
        row(1, ".2.", 3)
        row(4, 5, 6)
    }, """
    1 .2. 3
    4 5   6
    """)

    private fun doTest(expected: String, builder: TableBuilder.() -> Unit) {
        checkRender(table(builder), expected.trimMargin(), trimIndent = false)
    }

    private fun doBodyTest(expected: String, builder: SectionBuilder.() -> Unit) {
        checkRender(table {
            borderStyle = BorderStyle.HEAVY_HEAD_FOOT
            body(builder)
        }, expected.trimMargin(), trimIndent = false)
    }
}
