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
import com.github.ajalt.mordant.widgets.Text
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.string.shouldContain
import kotlin.js.JsName
import kotlin.test.Test

class TableLayoutTest : RenderingTest() {
    @[Test JsName("single_cell_with_spans")]
    fun `single cell with spans`() = doBodyTest(
        """
    ░┌───┐
    ░│ A │
    ░└───┘
    """
    ) {
        row {
            cell("A") {
                rowSpan = 2
                columnSpan = 3
            }
        }
    }

    @[Test JsName("single_row_multiple_spans")]
    fun `single row with multiple different spans`() = doBodyTest(
        """
    ░┌───┬───┬───┬───┐
    ░│ A │ B │ C │ D │
    ░└───┴───┴───┴───┘
    """
    ) {
        row {
            cell("A")
            cell("B") { columnSpan = 2 }
            cell("C") { columnSpan = 3 }
            cell("D")
        }
    }


    @[Test JsName("single_column_multiple_spans")]
    fun `single column with multiple different spans`() = doBodyTest(
        /* XXX: Html renders like this:
            ░┌───┐
            ░│ A │
            ░├───┤
            ░│ B │
            ░├───┤
            ░│ C │
            ░└───┘
         */
        """
        ░┌───┐    ░
        ░│ A │    ░
        ░├───┤    ░
        ░│ B │    ░
        ░│   ├───┐░
        ░│   │ C │░
        ░└───┴───┘░
        """
    ) {
        row("A")
        row {
            cell("B") { rowSpan = 2 }
        }
        row {
            cell("C") { rowSpan = 3 }
        }
    }

    @[Test JsName("basic_row_span")]
    fun `basic row span`() = doBodyTest(
        """
    ░┌───┬───┐
    ░│ A │ B │
    ░│   ├───┤
    ░│   │ C │
    ░└───┴───┘
    """
    ) {
        row {
            cell("A") { rowSpan = 2 }
            cell("B")
        }
        row("C")
    }

    @[Test JsName("basic_column_span")]
    fun `basic column span`() = doBodyTest(
        """
    ░┌───────┐
    ░│ A     │
    ░├───┬───┤
    ░│ B │ C │
    ░└───┴───┘
    """
    ) {
        row {
            cell("A") { columnSpan = 2 }
        }
        row("B", "C")
    }

    @[Test JsName("column_span_after_row_span")]
    fun `column span after row span`() = doBodyTest(
        """
    ░┌───┬───┐
    ░│ A │ B │
    ░│   ├───┤
    ░│   │ C │
    ░├───┴───┤
    ░│ D     │
    ░└───────┘
    """
    ) {
        row {
            cell("A") { rowSpan = 2 }
            cell("B")
        }
        row("C")
        row {
            cell("D") { columnSpan = 2 }
        }
    }

    // XXX: This should render, but currently throws an exception
//    @[Test JsName("nested_spans")]
//    fun `nested spans`() = doBodyTest(
//        """
//    ░┌───┬───────┬───┐
//    ░│ A │ B     │ C │
//    ░│   ├───┬───┤   │
//    ░│   │ D │ E │   │
//    ░│   ├───┴───┤   │
//    ░│   │ F     │   │
//    ░└───┴───────┴───┘
//    """
//    ) {
//        row {
//            cell("A") { rowSpan = 3 }
//            cell("B") { columnSpan = 2 }
//            cell("C") { rowSpan = 3 }
//        }
//        row {
//            cell("D")
//            cell("E")
//        }
//        row {
//            cell("F") { columnSpan = 2 }
//        }
//    }

    @[Test JsName("adjacent_row_spans")]
    fun `adjacent row spans`() = doBodyTest(
        """
    ░┌───┬───┬───┐
    ░│ A │ B │ C │
    ░│   │   ├───┤
    ░│   │   │ D │
    ░│   ├───┼───┤
    ░│   │ E │ F │
    ░└───┴───┴───┘
    """
    ) {
        row {
            cell("A") { rowSpan = 3 }
            cell("B") { rowSpan = 2 }
            cell("C")
        }
        row("D")
        row("E", "F")
    }

    @[Test JsName("adjacent_column_spans")]
    fun `adjacent column spans`() = doBodyTest(
        """
    ░┌───────┬───────┐
    ░│ A     │ B     │
    ░├───┬───┼───┬───┤
    ░│ C │ D │ E │ F │
    ░└───┴───┴───┴───┘
    """
    ) {
        row {
            cell("A") { columnSpan = 2 }
            cell("B") { columnSpan = 2 }
        }
        row("C", "D", "E", "F")
    }

    @[Test JsName("mixed_spans_multiple_rows")]
    fun `mixed spans multiple rows`() = doBodyTest(
        /*
        XXX: Should look like this:
        ░┌───┬───────┬───┐
        ░│ A │ B     │ C │
        ░│   ├───┬───┤   │
        ░│   │ D │ E │   │
        ░├───┴───┼───┼───┤
        ░│ F     │ G │ H │
        ░├───────┴───┼───┤
        ░│ I         │ J │
        ░└───────────┴───┘
         */
        """
    ░┌───┬───┬───┐    ░
    ░│ A │ B │ C │    ░
    ░│   ├───┤   ├───┐░
    ░│   │ D │   │ E │░
    ░├───┼───┼───┼───┘░
    ░│ F │ G │ H │    ░
    ░├───┴───┼───┤    ░
    ░│ I     │ J │    ░
    ░└───────┴───┘    ░
    """
    ) {
        row {
            cell("A") { rowSpan = 2 }
            cell("B") { columnSpan = 2 }
            cell("C") { rowSpan = 2 }
        }
        row {
            cell("D")
            cell("E")
        }
        row {
            cell("F") { columnSpan = 2 }
            cell("G")
            cell("H")
        }
        row {
            cell("I") { columnSpan = 3 }
            cell("J")
        }
    }

    @[Test JsName("empty_cells_with_spans")]
    fun `empty cells with spans`() = doBodyTest(
        """
    ░┌──┬───┬──┐
    ░│  │ A │  │
    ░│  ├───┤  │
    ░│  │ B │  │
    ░└──┴───┴──┘
    """
    ) {
        row {
            cell("") { rowSpan = 2 }
            cell("A")
            cell("") { rowSpan = 2 }
        }
        row("B")
    }

    @[Test JsName("maximum_column_span")]
    fun `maximum column span`() = doBodyTest(
        """
    ░┌───────────┐
    ░│ A         │
    ░├───┬───┬───┤
    ░│ B │ C │ D │
    ░└───┴───┴───┘
    """
    ) {
        row {
            cell("A") { columnSpan = 3 }
        }
        row("B", "C", "D")
    }

    @[Test JsName("maximum_row_span")]
    fun `maximum row span`() = doBodyTest(
        """
    ░┌───┬───┬───┐
    ░│ A │ B │ C │
    ░│   ├───┼───┤
    ░│   │ D │ E │
    ░│   ├───┼───┤
    ░│   │ F │ G │
    ░└───┴───┴───┘
    """
    ) {
        row {
            cell("A") { rowSpan = 3 }
            cell("B")
            cell("C")
        }
        row("D", "E")
        row("F", "G")
    }


    @[Test JsName("border_case_span_to_edge")]
    fun `border case span to edge`() = doBodyTest(
        """
    ░┌───┬───┬───┐
    ░│ A │ B │ C │
    ░├───┴───┼───┤
    ░│ D     │ E │
    ░├───────┼───┤
    ░│ F     │ G │
    ░└───────┴───┘
    """
    ) {
        row("A", "B", "C")
        row {
            cell("D") { columnSpan = 2 }
            cell("E")
        }
        row {
            cell("F") { columnSpan = 2 }
            cell("G")
        }
    }

    @[Test JsName("complex_spans_with_merging")]
    fun `complex spans with merging`() = doBodyTest(
        """
    ░┌───┬───┬───┬───┐
    ░│ A │ B │ C │ D │
    ░├───┼───┴───┼───┤
    ░│ E │ F     │ G │
    ░├───┼───────┼───┤
    ░│ H │ I     │ J │
    ░├───┼───────┴───┤
    ░│ K │ L         │
    ░└───┴───────────┘
    """
    ) {
        row("A", "B", "C", "D")
        row {
            cell("E")
            cell("F") { columnSpan = 2 }
            cell("G")
        }
        row {
            cell("H")
            cell("I") { columnSpan = 2 }
            cell("J")
        }
        row {
            cell("K")
            cell("L") { columnSpan = 3 }
        }
    }

    @[Test JsName("row_spans_in_multiple_columns")]
    fun `row spans in multiple columns`() = doBodyTest(
        """
    ░┌───┬───┬───┐
    ░│ A │ B │ C │
    ░│   ├───┤   │
    ░│   │ D │   │
    ░│   ├───┤   │
    ░│   │ E │   │
    ░└───┴───┴───┘
    """
    ) {
        row {
            cell("A") { rowSpan = 3 }
            cell("B")
            cell("C") { rowSpan = 3 }
        }
        row("D")
        row("E")
    }

    @[Test JsName("corner_spanning")]
    fun `corner spanning`() = doBodyTest(
        """
    ░┌───┬───┬───┐
    ░│ A │ B │ C │
    ░├───┼───┼───┤
    ░│ D │ E │ F │
    ░├───┼───┴───┤
    ░│ G │ H     │
    ░└───┴───────┘
    """
    ) {
        row("A", "B", "C")
        row("D", "E", "F")
        row {
            cell("G")
            cell("H") { columnSpan = 2 }
        }
    }

    @[Test JsName("combined_row_column_span")]
    fun `combined row column span`() = doBodyTest(
        /*XXX
        HTML renders like this:
            ░┌───┬───┬───┐
            ░│ A │ B │ C │
            ░├───┼───┴───┤
            ░│ D │ E     │
            ░├───┤       │
            ░│ F │       │
            ░└───┴───────┘
         */
        """
    ░┌───┬───┬───┐    ░
    ░│ A │ B │ C │    ░
    ░├───┼───┴───┤    ░
    ░│ D │ E     │    ░
    ░│   │       ├───┐░
    ░│   │       │ F │░
    ░└───┴───────┴───┘░
    """
    ) {
        row("A", "B", "C")
        row {
            cell("D") { rowSpan = 2 }
            cell("E") { rowSpan = 3; columnSpan = 2 }
        }
        row("F")
    }

    @[Test JsName("alternating_span_pattern")]
    fun `alternating span pattern`() = doBodyTest(
        /*
        XXX: should look like this:
        ░┌───┬───────┐
        ░│ A │ B     │
        ░├───┼───┬───┤
        ░│ C │ D │ E │
        ░├───┼───┼───┤
        ░│ F │ G │ H │
        ░└───┴───┴───┘
         */
        """
    ░┌───────┬─────┐░
    ░│ A     │ B   │░
    ░├───┬───┼───┬─┘░
    ░│ C │ D │ E │  ░
    ░├───┼───┼───┤  ░
    ░│ F │ G │ H │  ░
    ░└───┴───┴───┘  ░
    """
    ) {
        row {
            cell("A") { columnSpan = 2 }
            cell("B") { columnSpan = 2 }
        }
        row {
            cell("C")
            cell("D") { columnSpan = 2 }
            cell("E")
        }
        row {
            cell("F") { columnSpan = 2 }
            cell("G")
            cell("H")
        }
    }


    @[Test JsName("large_table_mixed_spans")]
    fun `large table mixed spans`() = doBodyTest(
        /* XXX Html renders this crazy style like:
         * ░┌───┬───┬───┬───┬───┐
         * ░│ A │ B │ C │ D │ E │
         * ░├───┼───┴───┼───┼───┤
         * ░│ F │ G     │ H │ I │
         * ░│   ├───────┤   │   │
         * ░│   │ J     │   │   │
         * ░│   ├───┬───┤   │   ├───┬───┐
         * ░│   │ K │ L │   │   │ M │ N │
         * ░└───┴───┴───┴───┘   ╵   ╵   ╵
         * So don't worry about the exact format here.
         */
        """
    ░┌───┬───┬───┬───┬───┐        ░
    ░│ A │ B │ C │ D │ E │        ░
    ░├───┼───┴───┼───┼───┤        ░
    ░│ F │ G     │ H │ I │        ░
    ░│   │       │   │   ├───────┐░
    ░│   │       │   │   │ J     │░
    ░│   ├───┬───┤   │   ├───┬───┤░
    ░│   │ K │ L │   │   │ M │ N │░
    ░└───┴───┴───┴───┴───┴───┴───┘░
    """
    ) {
        row("A", "B", "C", "D", "E")
        row {
            cell("F") { rowSpan = 4 }
            cell("G") { rowSpan = 2; columnSpan = 2 }
            cell("H") { rowSpan = 4 }
            cell("I") { rowSpan = 5 }
        }
        row {
            cell("J") { columnSpan = 2 }
        }
        row("K", "L", "M", "N")
    }

    /*
     * Html renders this crazy style like:
     *
     *  ░┌───┬───┐
     *  ░│ A │ B │
     *  ░├───┘   ├───┐
     *  ░│ D     │ E │
     *  ░└────   ╵   ╵
     *  So don't worry about the exact format here.
     */
    @[Test JsName("multiple_truncated_spans")]
    fun `multiple truncated spans`() = doBodyTest(
        """
    ░┌───┬───┐    ░
    ░│ A │ B │    ░
    ░│   │   │    ░
    ░├   ───┤   │    ░
    ░│ C │   │    ░
    ░├───┤   ├───┐░
    ░│ D │   │ E │░
    ░   └───┴───┴───┘░
    """
    ) {
        row {
            cell("A") { rowSpan = 2 }
            cell("B") { rowSpan = 8; columnSpan = 5 }
        }
        row()
        row("C")
        row {
            cell("D") { columnSpan = 10 }
            cell("E")
        }
    }

    @[Test JsName("table_with_sparse_bottom")]
    fun `table with sparse bottom`() = doBodyTest(
        """
    ░┌───┬───┬───┬───┐
    ░│ A │ B │ C │ D │
    ░├───┼───┼───┼───┤
    ░│ E │ F │ G │ H │
    ░├───┤   │   │   │
    ░│ I │   │   │   │
    ░└───┴───┴───┴───┘
    """
    ) {
        row("A", "B", "C", "D")
        row {
            cell("E")
            for (c in listOf("F", "G", "H")) {
                cell(c) { rowSpan = 2 }
            }
        }
        row("I")
    }

    @[Test JsName("span_truncation_non_square_table")]
    fun `span truncation non square table`() = doBodyTest(
        """
    ░┌───┬───┬───┐░
    ░│ A │ B │ C │░
    ░├───┼───┼───┘░
    ░│ D │ E │    ░
    ░├───┴───┴───┐░
    ░│ F         │░
    ░└───────────┘░
    """
    ) {
        row("A", "B", "C")
        row("D", "E")
        row {
            cell("F") { columnSpan = 9 }
        }
    }

    @[Test JsName("extreme_shape_difference")]
    fun `extreme shape difference`() = doBodyTest(
        """
    ░┌───┐                            ░
    ░│ A │                            ░
    ░├───┤                            ░
    ░│ B │                            ░
    ░├───┤                            ░
    ░│ C │                            ░
    ░├───┼───┬───┬───┬───┬───┬───┬───┐░
    ░│ D │ E │ F │ G │ H │ I │ J │ K │░
    ░└───┴───┴───┴───┴───┴───┴───┴───┘░
    """
    ) {
        row("A")
        row("B")
        row("C")
        row("D", "E", "F", "G", "H", "I", "J", "K")
    }

    @[Test JsName("overlapping_spans")]
    fun `overlapping spans`() {
        shouldThrow<IllegalArgumentException> {
            checkRender(table {
                body {
                    row {
                        cell("A")
                        cell("B") { rowSpan = 2 }
                    }
                    row {
                        cell("C") { columnSpan = 2 }
                    }
                }
            }, "")
        }.message shouldContain "cell spans cannot overlap"
    }

    private fun doTest(expected: String, width: Int = 79, builder: TableBuilder.() -> Unit) {
        checkRender(table(builder), expected, width = width, printWithIndent = "    ░")
    }

    private fun doBodyTest(expected: String, width: Int = 79, builder: SectionBuilder.() -> Unit) {
        doTest(expected, width) {
            borderType = BorderType.HEAVY_HEAD_FOOT
            body(builder)
        }
    }
}
