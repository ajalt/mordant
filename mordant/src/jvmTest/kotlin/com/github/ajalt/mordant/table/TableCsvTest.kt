package com.github.ajalt.mordant.table

import com.github.ajalt.mordant.rendering.Whitespace.PRE
import com.github.ajalt.mordant.widgets.Text
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import org.junit.Test
import kotlin.test.assertEquals

class TableCsvTest {
    private fun t(row: Iterable<Any>): Table {
        return table { body { rowFrom(row) } }
    }

    @Test
    fun `empty table`() {
        t(emptyList()).contentToCsv() shouldBe "\n"
    }

    @Test
    fun `single cell table`() {
        t(listOf(1)).contentToCsv() shouldBe "1\n"
    }

    @Test
    fun escaping() = forAll(
        row(listOf("a", 1, "p,q"), "a,1,\"p,q\"", '\\', true, CsvQuoting.MINIMAL),
        row(listOf("a", 1, "p,\"q\""), "a,1,\"p,\\\"q\\\"\"", '\\', false, CsvQuoting.MINIMAL),
        row(listOf("\""), "\"\"\"\"", '\\', true, CsvQuoting.MINIMAL),
        row(listOf("\""), "\\\"", '\\', false, CsvQuoting.MINIMAL),
        row(listOf("\""), "\\\"", '\\', true, CsvQuoting.NONE),
        row(listOf("a", 1, "p,q"), "a,1,p\\,q", '\\', true, CsvQuoting.NONE),
    ) { row, expected, escapeChar, doubleQuote, quoting ->
        t(row).contentToCsv(
            escapeChar = escapeChar,
            doubleQuote = doubleQuote,
            quoting = quoting
        ) shouldBe expected + "\n"
    }

    @Test
    fun `escape error`() {
        shouldThrow<IllegalArgumentException> {
            t(listOf("a", 1, "p,\"q\"")).contentToCsv(
                escapeChar = null,
                doubleQuote = false,
            )
        }
    }

    @Test
    fun quoting() = forAll(
        row(listOf("a", 1, "p,q"), "a,1,\"p,q\"", CsvQuoting.MINIMAL),
        row(listOf("a", 1, "p,q"), "\"a\",1,\"p,q\"", CsvQuoting.NONNUMERIC),
        row(listOf("a", 1, "p,q"), "\"a\",\"1\",\"p,q\"", CsvQuoting.ALL),
        row(listOf(Text("a\nb", whitespace = PRE), 1), "\"a\nb\",\"1\"", CsvQuoting.ALL),
    ) { row, expected, quoting ->
        t(row).contentToCsv(quoting = quoting) shouldBe expected + "\n"
    }

    @Test
    fun `quoting none`() {
        shouldThrow<IllegalArgumentException> {
            t(listOf("a", 1, "p,q")).contentToCsv(quoting = CsvQuoting.NONE)
        }
    }

    @Test
    fun `column span`() {
        table {
            body {
                row {
                    cell(1) { columnSpan = 2 }
                    cell(2)
                }
                row(3, 4, 5)
            }
        }.contentToCsv() shouldBe """
        |1,,2
        |3,4,5
        |
        """.trimMargin()
    }

    @Test
    fun `row span`() {
        table {
            body {
                row {
                    cell(1) { rowSpan = 2 }
                    cell(2)
                }
                row(3)
            }
        }.contentToCsv() shouldBe """
        |1,2
        |,3
        |
        """.trimMargin()
    }
}
