package com.github.ajalt.mordant.samples

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.BorderStyle.Companion.SQUARE_DOUBLE_SECTION_SEPARATOR
import com.github.ajalt.mordant.rendering.TextAlign.LEFT
import com.github.ajalt.mordant.rendering.TextAlign.RIGHT
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.table.Borders.ALL
import com.github.ajalt.mordant.table.Borders.TOM_BOTTOM
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal


fun main() {
    val terminal = Terminal(AnsiLevel.TRUECOLOR)

    val table = table {
        outerBorder = false
        borderStyle = SQUARE_DOUBLE_SECTION_SEPARATOR
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
            borders = TOM_BOTTOM
            column(0) {
                style = TextStyle(dim = true)
                borders = ALL
            }
            column(3) {
                style = TextStyle(dim = true)
                borders = ALL
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
        captionBottom("Budget courtesy @dril", TextStyle(dim = true))
    }

    terminal.print(table)
}
