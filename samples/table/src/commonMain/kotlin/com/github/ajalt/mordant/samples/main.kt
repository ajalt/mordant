package com.github.ajalt.mordant.samples

import com.github.ajalt.mordant.rendering.BorderType.Companion.SQUARE_DOUBLE_SECTION_SEPARATOR
import com.github.ajalt.mordant.rendering.TextAlign.LEFT
import com.github.ajalt.mordant.rendering.TextAlign.RIGHT
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.table.Borders.*
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal


fun main() {
    val terminal = Terminal()

    val table = table {
        tableBorders = NONE
        borderType = SQUARE_DOUBLE_SECTION_SEPARATOR
        align = RIGHT
        column(0) {
            align = LEFT
            style = magenta
        }
        column(3) {
            style = magenta
        }
        header {
            style = magenta
            row("", "Projected Cost", "Actual Cost", "Difference")
        }
        body {
            cellBorders = TOP_BOTTOM
            column(0) {
                style = TextStyle(bold = true)
                cellBorders = ALL
            }
            column(3) {
                style = TextStyle(bold = true)
                cellBorders = ALL
            }
            rowStyles(blue, brightBlue)

            row("Food", "$400", "$200", "$200")
            row("Data", "$100", "$150", "$-50")
            row("Rent", "$800", "$800", "$0")
            row("Candles", "$0", "$3,600", "$-3,600")
            row("Utility", "$154", "$150", "$-5")
        }
        footer {
            row {
                cell("Subtotal")
                cell("$-3,455") {
                    columnSpan = 3
                }
            }
        }
        captionBottom(dim("Budget courtesy @dril"))
    }

    terminal.println(table)
}
