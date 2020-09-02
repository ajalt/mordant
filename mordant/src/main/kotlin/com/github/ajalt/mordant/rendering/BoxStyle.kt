package com.github.ajalt.mordant.rendering


data class BoxRowStyle(
        val left: String, val mid: String, val divider: String, val right: String
) {
    internal companion object {
        fun build(it: String) = BoxRowStyle(it[0].toString(), it[1].toString(), it[2].toString(), it[3].toString())
    }
}

class BoxStyle(
        val top: BoxRowStyle,
        val head: BoxRowStyle,
        val headDivider: BoxRowStyle,
        val body: BoxRowStyle,
        val bodyDivider: BoxRowStyle,
        val footDivider: BoxRowStyle,
        val foot: BoxRowStyle,
        val bottom: BoxRowStyle
) {
    companion object {
        private fun build(string: String): BoxStyle {
            val lines = string.trimIndent().lines()
            return BoxStyle(
                    top = BoxRowStyle.build(lines[0]),
                    head = BoxRowStyle.build(lines[1]),
                    headDivider = BoxRowStyle.build(lines[2]),
                    body = BoxRowStyle.build(lines[3]),
                    bodyDivider = BoxRowStyle.build(lines[4]),
                    footDivider = BoxRowStyle.build(lines[5]),
                    foot = BoxRowStyle.build(lines[6]),
                    bottom = BoxRowStyle.build(lines[7]),
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
