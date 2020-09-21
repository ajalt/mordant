package com.github.ajalt.mordant.rendering.table

import com.github.ajalt.mordant.AnsiLevel
import com.github.ajalt.mordant.Terminal
import io.kotest.matchers.shouldBe
import org.junit.Test

class TableTest {
    private val t = Terminal(level = AnsiLevel.TRUECOLOR)


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

    private fun doBodyTest(expected: String, builder: SectionBuilder.() -> Unit) = doTest(expected) { body(builder) }
    private fun doTest(expected: String, builder: TableBuilder.() -> Unit) {
        val actual = t.render(table(builder))
        try {
            actual shouldBe expected.trimMargin()
        } catch (e: Throwable) {
            println(actual)
            throw e
        }
    }
}
