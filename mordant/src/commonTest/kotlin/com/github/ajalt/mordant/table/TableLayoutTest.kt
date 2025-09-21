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

    @[Test JsName("single_row_spans")]
    fun `single row with spans`() = doBodyTest(
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
            cell("D") { columnSpan = 4 }
        }
    }


    @[Test JsName("single_column_spans")]
    fun `single column with spans`() = doBodyTest(
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
        ░┌───┐        ░
        ░│ A │        ░
        ░│   ├───┐    ░
        ░│   │ B │    ░
        ░│   │   ├───┐░
        ░│   │   │ C │░
        ░└───┴───┴───┘░
        """
    ) {
        row {
            cell("A") { rowSpan = 3 }
        }
        row {
            cell("B") { rowSpan = 3 }
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

    @[Test JsName("column_span_with_adjacent_row_span")]
    fun `column span with adjacent row span`() = doBodyTest(
        /* XXX HTML renders like this:
            ░┌───────┬───┐
            ░│ A     │ B │
            ░├───┬───┤   │
            ░│ C │ D │   │
            ░└───┴───┴───┘
         */
        """
    ░┌───┬───┐    ░
    ░│ A │ B │    ░
    ░├───┤   ├───┐░
    ░│ C │   │ D │░
    ░└───┴───┴───┘░
    """
    ) {
        row {
            cell("A") { columnSpan = 2 }
            cell("B") { rowSpan = 2 }
        }
        row("C", "D")
    }

    @[Test JsName("row_span_with_adjacent_cell_in_second_row")]
    fun `row span with adjacent cell in second row`() = doBodyTest(
        """
    ░┌───┬───┐    ░
    ░│ A │ B │    ░
    ░├───┤   ├───┐░
    ░│ C │   │ D │░
    ░└───┴───┴───┘░
    """
    ) {
        row {
            cell("A")
            cell("B") { rowSpan = 2 }
        }
        row("C", "D")
    }

    @[Test JsName("column_span_with_unaligned_cells_below")]
    fun `column span with unaligned cells below`() = doBodyTest(
        /* XXX HTML renders like this:
        ░┌───────┬───┐░
        ░│ A     │ B │░
        ░├───┬───┼───┘░
        ░│ C │ D │    ░
        ░└───┴───┘    ░
         */

        """
    ░┌───┬───┐
    ░│ A │ B │
    ░├───┼───┤
    ░│ C │ D │
    ░└───┴───┘
    """
    ) {
        row {
            cell("A") { columnSpan = 2 }
            cell("B")
        }
        row {
            cell("C")
            cell("D")
        }
    }

    // XXX: This should render, but currently throws an exception
//    @[Test JsName("nested_spans_truncated")]
//    fun `nested spans truncated`() = doBodyTest(
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


    @[Test JsName("colspan_before_and_after_cells")]
    fun `colspan before and after cells`() = doBodyTest(
        /* XXX: Html renders like this:
        ░┌───┬───────┬───┐
        ░│ A │ B     │ C │
        ░│   ├───┬───┤   │
        ░│   │ D │ E │   │
        ░└───┴───┴───┴───┘
         */
        """
    ░┌───┬───┬───┐    
    ░│ A │ B │ C │    
    ░│   ├───┤   ├───┐
    ░│   │ D │   │ E │
    ░└───┴───┴───┴───┘
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
            cell("A") { columnSpan = 9 }
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
            cell("A") { rowSpan = 9 }
            cell("B")
            cell("C")
        }
        row("D", "E")
        row("F", "G")
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

    @[Test JsName("combined_row_column_span")]
    fun `combined row column span`() = doBodyTest(
        /*XXX HTML renders like this:
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
        /* XXX Html renders like this:
        ░┌───────┬───┐
        ░│ A     │ B │
        ░├───┬───┼───┤
        ░│ C │ D │ E │
        ░└───┴───┴───┘

        This issue is caused because the table width is calculated as 4 because B's
        columnSpan is left at 2 instead of being truncated in insertCell.
         */
        """
    ░┌───────┬─────┐░
    ░│ A     │ B   │░
    ░├───┬───┼───┬─┘░
    ░│ C │ D │ E │  ░
    ░└───┴───┴───┘  ░
    """
    ) {
        row {
            cell("A") { columnSpan = 2 }
            cell("B") { columnSpan = 2 }
        }
        row {
            cell("C")
            cell("D")
            cell("E")
        }
    }


    @[Test JsName("large_table_mixed_spans")]
    fun `large table mixed spans`() = doBodyTest(
        /* XXX Html renders this like:
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
    ░├───┤   │    ░
    ░│ C │   │    ░
    ░├───┤   ├───┐░
    ░│ D │   │ E │░
    ░└───┴───┴───┘░
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

    @[Test JsName("row_span_custom_borders")]
    fun `row span custom borders`() = doBodyTest(
        """
    ░┌───┬───┐
    ░│ 1 │ 2 │
    ░│   │ 3 │
    ░├───┼───┤
    ░│ 4 │ 5 │
    ░└───┴───┘
    """
    ) {
        cellBorders = LEFT_RIGHT
        row {
            cellBorders = LEFT_TOP_RIGHT
            cell("1") { rowSpan = 2 }
            cell(2)
        }
        row(3)
        row {
            cellBorders = ALL
            cells(4, 5)
        }
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
