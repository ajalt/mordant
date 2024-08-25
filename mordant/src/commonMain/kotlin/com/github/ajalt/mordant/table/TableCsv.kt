package com.github.ajalt.mordant.table

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalRecorder
import com.github.ajalt.mordant.widgets.Padded
import com.github.ajalt.mordant.widgets.Text
import com.github.ajalt.mordant.widgets.withAlign

enum class CsvQuoting {
    /** All fields will be quoted */
    ALL,

    /**
     * Only fields containing the `delimiter`, the `quotechar`, or any characters in the
     * `lineterminator` will be quoted
     */
    MINIMAL,

    /** All fields containing non-numeric characters will be quoted */
    NONNUMERIC,

    /**
     * Fields will never be quoted. If a special character (a delimeter, quote, or line terminator)
     * is present in a field, it will be preceded by an `escapechar` if one is specified, or an
     * [IllegalArgumentException] will be thrown in `escapechar` is null.
     */
    NONE
}

/**
 * @param delimiter The character used to separate fields
 * @param quoteChar The character used to surround fields that contain any of the special
 *   characters: [delimiter], [quoteChar], or [lineTerminator]
 * @param escapeChar The character placed before special characters when [quoting] is
 *   [NONE][CsvQuoting.NONE] or [doubleQuote] is `false`
 * @param doubleQuote If `true`, occurrences of [quoteChar] inside a quoted field will be doubled.
 *   If `false`, they will be prefixed by [escapeChar], or an [IllegalArgumentException] will be
 *   thrown if [escapeChar] is `null`.
 * @param lineTerminator The character used to terminate lines
 * @param quoting The quoting method to use
 */
fun Table.contentToCsv(
    delimiter: Char = ',',
    quoteChar: Char = '"',
    escapeChar: Char? = null,
    doubleQuote: Boolean = false,
    lineTerminator: String = "\n",
    quoting: CsvQuoting = CsvQuoting.MINIMAL,
): String = buildString {
    val rows = getContentRows()
    val escapable = Regex("[$delimiter$lineTerminator]")

    for (row in rows) {
        row.joinTo(this, ",") { cell ->
            val escapesEscaped = escapeChar?.let { cell.replace(it.toString(), "$it$it") } ?: cell

            require(doubleQuote || escapeChar != null || quoteChar !in cell) { "Content requires escaping, but no escapeChar set" }

            val quotesEscaped = escapesEscaped.replace(
                quoteChar.toString(),
                if (doubleQuote && quoting != CsvQuoting.NONE) "$quoteChar$quoteChar" else "$escapeChar$quoteChar"
            )

            val allEscaped = when (quoting) {
                CsvQuoting.NONE -> {
                    require(escapeChar != null || !escapable.containsMatchIn(cell)) { "Content requires escaping, but no escapeChar set" }
                    escapable.replace(quotesEscaped) { "$escapeChar${it.value}" }
                }

                else -> quotesEscaped
            }

            val needsQuote = when (quoting) {
                CsvQuoting.ALL -> true
                CsvQuoting.MINIMAL -> {
                    (escapeChar == null || doubleQuote) && quoteChar in cell
                            || escapable.containsMatchIn(cell)
                }

                CsvQuoting.NONNUMERIC -> cell.any { it !in '0'..'9' }
                CsvQuoting.NONE -> false
            }
            if (needsQuote) "\"$allEscaped\"" else allEscaped
        }
        append(lineTerminator)
    }
}

private fun Table.getContentRows(): List<List<String>> {
    val rows = when (this) {
        is TableImpl -> rows
        is TableWithCaption -> table.rows
    }

    val t = Terminal(
        terminalInterface = TerminalRecorder(
            ansiLevel = AnsiLevel.NONE,
            width = Int.MAX_VALUE,
            height = Int.MAX_VALUE,
            hyperlinks = false,
            outputInteractive = false,
            inputInteractive = false,
        )
    )

    return rows.map { row ->
        row.map { cell ->
            when (cell) {
                is Cell.Empty -> "" // Shouldn't happen, only used during layout
                is Cell.SpanRef -> ""
                is Cell.Content -> {
                    val widget = cell.content.let { if (it is Padded) it.content else it }
                    require(widget is Text) { "Only Text widgets can be rendered as csv" }
                    t.render(widget.withAlign(TextAlign.NONE))
                }
            }
        }
    }
}
