package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.AnsiColor.blue
import com.github.ajalt.mordant.AnsiColor.red
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.TextAlign.CENTER
import com.github.ajalt.mordant.rendering.VerticalAlign.MIDDLE
import com.github.ajalt.mordant.rendering.Whitespace.PRE
import com.github.ajalt.mordant.rendering.table.Borders.*
import org.junit.Test

class TableTest : RenderingTest() {
    @Test
    fun `empty cell`() = doTest("""
    |┌┐
    |││
    |└┘
    """) {
        padding = Padding.none()
        row("")
    }

    @Test
    fun `border top`() = doTest("""
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
    fun `border right`() = doTest("""
    | 1 │
    """) {
        row {
            cell(1) {
                borders = RIGHT
            }
        }
    }

    @Test
    fun `border bottom`() = doTest("""
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
    fun `border left`() = doTest("""
    |│ 1 ⏎
    """) {
        row {
            cell(1) {
                borders = LEFT
            }
        }
    }

    @Test
    fun `border top with corners`() = doTest("""
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
    fun `border right with corners`() = doTest("""
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
    fun `border bottom with corners`() = doTest("""
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
    fun `border left with corners`() = doTest("""
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
    fun `inside borders`() = doTest("""
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
    fun `empty row`() = doTest("""
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
    fun `non-rectangular table`() = doTest("""
    |┌───┐   
    |│ 1 │    
    |├───┼───┐
    |│ 2 │ 3 │
    |├───┼───┘
    |│ 4 │    
    |└───┘   
    """) {
        row(1)
        row(2,3)
        row(4)
    }

    @Test
    fun `preformatted text content`() = doTest("""
    |┌────────────────┬─┐
    |│line 1          │2│
    |│2nd line no wrap│ │
    |├────────────────┼─┤
    |│3               │4│
    |└────────────────┴─┘
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
    fun `wide unicode characters`() = doTest("""
    |┌──────────┐
    |│ 모ㄹ단ㅌ │
    |├──────────┤
    |│   媒人   │
    |├──────────┤
    |│  🙊🙉🙈  │
    |├──────────┤
    |│    .     │
    |└──────────┘
    """) {
        align = CENTER
        row("모ㄹ단ㅌ")
        row("媒人")
        row("🙊🙉🙈")
        row(".")
    }

    @Test
    fun `striped row styles`() = doTest("""
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
        padding = Padding.none()
        row("row 1")
        row("row 2")
        row("row 3")
        row("row 4")
    }

    @Test
    fun `row and column span no borders`() = doTest("""
    |span1
    |    2
    |3 4 5
    """) {
        borders = NONE
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
    fun `row and column span`() = doTest("""
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
    fun `nested tables`() = doTest("""
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
    fun `outer border`() = checkRender(table {
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
    }, """
     1  │ 2  │ 3  
    ────┼────┼────
     4  │ 5  │ 6  
     7  │ 8  │ 9  
    ────┼────┼────
     11 │ 12 │ 13 
    """)

    private fun doTest(expected: String, builder: SectionBuilder.() -> Unit) {
        checkRender(table {
            borderStyle = BorderStyle.HEAVY_HEAD_FOOT
            body(builder)
        }, expected.trimMargin(), trimIndent = false)
    }
}
