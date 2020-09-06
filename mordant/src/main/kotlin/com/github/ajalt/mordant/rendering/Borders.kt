package com.github.ajalt.mordant.rendering


data class BorderRow(
        val left: String, val mid: String, val divider: String, val right: String
) {
    internal companion object {
        fun build(it: String) = BorderRow(it[0].toString(), it[1].toString(), it[2].toString(), it[3].toString())
    }
}

class Borders(
        val top: BorderRow,
        val head: BorderRow,
        val headDivider: BorderRow,
        val body: BorderRow,
        val bodyDivider: BorderRow,
        val footDivider: BorderRow,
        val foot: BorderRow,
        val bottom: BorderRow
) {
    fun renderTop(widths: List<Int>, style: TextStyle = DEFAULT_STYLE): Span = row(top, widths, style)
    fun renderHead(widths: List<Int>, style: TextStyle = DEFAULT_STYLE): Span = row(headDivider, widths, style)
    fun renderBody(widths: List<Int>, style: TextStyle = DEFAULT_STYLE): Span = row(bodyDivider, widths, style)
    fun renderFoot(widths: List<Int>, style: TextStyle = DEFAULT_STYLE): Span = row(footDivider, widths, style)
    fun renderBottom(widths: List<Int>, style: TextStyle = DEFAULT_STYLE): Span = row(bottom, widths, style)

    private fun row(border: BorderRow, widths: List<Int>, style: TextStyle): Span {
        require(widths.isNotEmpty()) { "Must provide at least one width" }

        val text = buildString {
            append(border.left)
            for ((i, width) in widths.withIndex()) {
                if (i > 0) append(border.divider)
                repeat(width) { append(border.mid) }
            }
            append(border.right)
        }
        return Span.word(text, style)
    }

    companion object {
        private fun build(string: String): Borders {
            val lines = string.trimIndent().lines()
            return Borders(
                    top = BorderRow.build(lines[0]),
                    head = BorderRow.build(lines[1]),
                    headDivider = BorderRow.build(lines[2]),
                    body = BorderRow.build(lines[3]),
                    bodyDivider = BorderRow.build(lines[4]),
                    footDivider = BorderRow.build(lines[5]),
                    foot = BorderRow.build(lines[6]),
                    bottom = BorderRow.build(lines[7]),
            )
        }

        val ASCII = build(
                """
                +-++
                | ||
                +-++
                | ||
                +-++
                +-++
                | ||
                +-++
                """
        )

        val SQUARE = build(
                """
                ┌─┬┐
                │ ││
                ├─┼┤
                │ ││
                ├─┼┤
                ├─┼┤
                │ ││
                └─┴┘
                """
        )
    }
}
