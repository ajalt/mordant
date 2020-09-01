package com.github.ajalt.mordant.rendering

import com.github.ajalt.colormath.Ansi16
import com.github.ajalt.colormath.Ansi256
import com.github.ajalt.colormath.ConvertibleColor
import com.github.ajalt.colormath.RGB
import com.github.ajalt.mordant.AnsiCodes
import com.github.ajalt.mordant.ESC

data class Span(val text: String, val style: TextStyle = TextStyle()) {
    companion object {
        fun line() = Span("\n")
    }

    val cellWidth: Int = text.length // TODO: calculate cell width


    fun withStyle(style: TextStyle) = copy(style = style + this.style)


    /** Split span into words, spaces, and newlines*/
    internal fun split(collapseNewlines: Boolean): Sequence<Span> {
        val re = if (collapseNewlines) SPLIT_REGEX_COLLAPSE_NL else SPLIT_REGEX
        return re.findAll(text).flatMap { parseAnsi(it.value) }
    }

    private fun parseAnsi(text: String): List<Span> {
        val commands = ANSI_RE.findAll(text).toList()
        if (commands.isEmpty()) return listOf(copy(text = text))

        val parts = mutableListOf<Span>()
        var i = 0
        var style = TextStyle()
        for (command in commands) {
            if (i > 0) {
                parts += copy(text = text.substring(i, command.range.first), style = this.style + style)
            }
            i = command.range.last + 1
            style = if (command.value.endsWith("m")) styleFromAnsi(command.value) else TextStyle()
        }
        return parts
    }
}

private fun styleFromAnsi(string: String): TextStyle {
    val codes = string.subSequence(2, string.length - 1)
            .split(";").map { if (it.isEmpty()) 0 else it.toInt() }

    if (codes.isEmpty()) return TextStyle()

    var style = TextStyle()
    var i = 0

    while (i <= codes.lastIndex) {
        when (val code = codes[i]) {
            in AnsiCodes.fg16Range, in AnsiCodes.fg16BrightRange -> {
                style = style.copy(color = Ansi16(code))
            }
            in AnsiCodes.bg16Range, in AnsiCodes.bg16BrightRange -> {
                style = style.copy(bgColor = Ansi16(code - AnsiCodes.fgBgOffset))
            }
            AnsiCodes.fgColorSelector -> {
                val (color, consumed) = getAnsiColor(i + 1, codes)
                if (color == null) break // Unrecognized code format: stop parsing
                style = style.copy(color = color)
                i += consumed
            }
            AnsiCodes.bgColorSelector -> {
                val (color, consumed) = getAnsiColor(i + 1, codes)
                if (color == null) break
                style = style.copy(bgColor = color)
                i += consumed
            }
            AnsiCodes.boldOpen -> style = style.copy(bold = true)
            AnsiCodes.italicOpen -> style = style.copy(italic = true)
            AnsiCodes.underlineOpen -> style = style.copy(underline = true)
            AnsiCodes.dimOpen -> style = style.copy(dim = true)
            AnsiCodes.inverseOpen -> style = style.copy(inverse = true)
            AnsiCodes.strikethroughOpen -> style = style.copy(strikethrough = true)
            AnsiCodes.underlineColorSelector -> {
                // Not supported, skip its arguments
                i += when {
                    i == codes.lastIndex -> 0
                    codes[i + 1] == AnsiCodes.selector256 -> 1
                    codes[i + 1] == AnsiCodes.selectorRgb -> 3
                    else -> break
                }
            }
        }
        i += 1
    }

    return style
}

private fun getAnsiColor(i: Int, codes: List<Int>): Pair<ConvertibleColor?, Int> {
    return when (codes[i]) {
        AnsiCodes.selector256 -> {
            if (i >= codes.lastIndex || codes[i + 1] in 0..255) {
                Ansi256(codes[i + 1]) to 1
            } else {
                null to 0
            }
        }
        AnsiCodes.selectorRgb -> {
            if (i + 3 >= codes.lastIndex
                    || codes[i + 1] !in 0..255
                    || codes[i + 2] !in 0..255
                    || codes[i + 3] !in 0..255) {
                null to 0
            } else {
                RGB(codes[i + 1], codes[i + 2], codes[i + 3]) to 3
            }
        }
        else -> null to 0
    }
}

private val SPLIT_REGEX = Regex("""\r?\n|\s+|\S+""")
private val SPLIT_REGEX_COLLAPSE_NL = Regex("""\s+|\S+""")
private val ANSI_RE = Regex("""$ESC(?:[@-Z\\-_]|\[[0-?]*[ -/]*[@-~])""")
