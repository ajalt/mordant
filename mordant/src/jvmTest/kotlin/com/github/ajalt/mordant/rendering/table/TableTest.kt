package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.rendering.*
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
        align = TextAlign.CENTER
        row("모ㄹ단ㅌ")
        row("媒人")
        row("🙊🙉🙈")
        row(".")
    }


    private fun doTest(expected: String, builder: SectionBuilder.() -> Unit) {
        checkRender(table {
            borderStyle = BorderStyle.HEAVY_HEAD_FOOT
            body(builder)
        }, expected.trimMargin(), trimIndent = false)
    }
}
