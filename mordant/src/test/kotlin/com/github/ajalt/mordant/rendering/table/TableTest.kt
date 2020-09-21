package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.rendering.RenderingTest
import org.junit.Test

class TableTest : RenderingTest() {
    @Test
    fun `border top`() = doBodyTest("""
    |───
    | 1 
    """) {
        row {
            cell(1) {
                borders = Borders(top = true)
            }
        }
    }

    @Test
    fun `border right`() = doBodyTest("""
    | 1 │
    """) {
        row {
            cell(1) {
                borders = Borders(right = true)
            }
        }
    }

    @Test
    fun `border bottom`() = doBodyTest("""
    | 1 
    |───
    """) {
        row {
            cell(1) {
                borders = Borders(bottom = true)
            }
        }
    }

    @Test
    fun `border left`() = doBodyTest("""
    |│ 1 
    """) {
        row {
            cell(1) {
                borders = Borders(left = true)
            }
        }
    }

    @Test
    fun `border top with corners`() = doBodyTest("""
    |╶───╴
    |  1  
    |┌───┐
    |│ 2 │
    |└───┘
    """) {
        row {
            cell(1) {
                borders = Borders(top = true)
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
                borders = Borders(right = true)
            }
        }
    }

    @Test
    fun `border bottom with corners`() = doBodyTest("""
    |┌───┐
    |│ 1 │
    |└───┘
    |  2  
    |╶───╴
    """) {
        row(1)
        row {
            cell(2) {
                borders = Borders(bottom = true)
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
                borders = Borders(left = true)
            }
            cell(2)
        }
    }

    @Test
    fun `inside borders`() = doBodyTest("""
    | 1 │ 2 
    |───┼───
    | 3 │ 4 
    """) {
        row {
            cell(1) {
                borders = Borders(right = true)
            }
            cell(2) {
                borders = Borders(bottom = true)
            }
        }
        row {
            cell(3) {
                borders = Borders(top=true)
            }
            cell(4) {
                borders = Borders(left=true)
            }
        }
    }


    @Test
    fun `empty row`() = doBodyTest("""
    |┌───┐
    |│ 1 │
    |└───┘
    |     
    |┌───┐
    |│ 2 │
    |└───┘
    """) {
        row(1)
        row()
        row(2)
    }


    private fun doBodyTest(expected: String, builder: SectionBuilder.() -> Unit) {
        doTest(table { body(builder) }, expected)
    }
}
