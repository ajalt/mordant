package com.github.ajalt.mordant.rendering.internal

import com.github.ajalt.colormath.Ansi16
import com.github.ajalt.colormath.Ansi256
import com.github.ajalt.colormath.ConvertibleColor
import com.github.ajalt.colormath.RGB
import com.github.ajalt.mordant.*
import com.github.ajalt.mordant.rendering.*

private val ANSI_RE = Regex("""$ESC(?:[@-Z\\-_]|\[[0-?]*[ -/]*[@-~])""")

/** Like a Span, but with no restrictions on [text] */
private data class Chunk(val text: String, val style: TextStyle)

/**
 * Split [text] into [Span]s, stripping ANSI codes and converting them to [TextStyle]s.
 *
 * Unknown ANSI codes are discarded.
 */
internal fun parseText(text: String, style: TextStyle): Lines {
    val parseAnsi = parseAnsi(text, style)
    val words = parseAnsi.flatMap { splitWords(it) }.toList()
    val splitLines = splitLines(words)
    return Lines(splitLines)
}

private fun parseAnsi(text: String, defaultStyle: TextStyle): List<Chunk> {
    val commands = ANSI_RE.findAll(text).toList()
    if (commands.isEmpty()) return listOf(Chunk(text, defaultStyle))

    val parts = mutableListOf<Chunk>()
    var idxAfterLastCmd = 0
    var style = defaultStyle
    for (command in commands) {
        if (command.range.first > idxAfterLastCmd) {
            parts += Chunk(text = text.substring(idxAfterLastCmd, command.range.first), style = style)
        }
        idxAfterLastCmd = command.range.last + 1
        style = updateStyle(style, command.value)
    }
    if (idxAfterLastCmd < text.length) {
        parts += Chunk(text = text.substring(idxAfterLastCmd), style = style)
    }
    return parts
}

// This could be implemented as a one line regex, but regex engines don't behave the same across
// platforms, especially when dealing with unicode, which we need.
private fun splitWords(chunk: Chunk): List<Chunk> {
    val chunks = mutableListOf<Chunk>()
    var i = 0
    var start = 0
    var chunkType = -1 // 0=newline, 1=tab, 2=space, 3=word
    val t = chunk.text
    while (i < t.length) {
        val c = t[i]
        val type = when {
            c == '\r' || c == '\n' -> 0
            c == '\t' -> 1
            c.isWhitespace() -> 2
            else -> 3
        }
        if (i == 0) {
            chunkType = type
        } else if (c == '\n' || c == '\t' || chunkType != type) {
            chunks += chunk.copy(text = t.substring(start, i))
            start = i
            chunkType = type
        }
        i += 1
    }
    if (start != i) {
        chunks += chunk.copy(text = t.substring(start, i))
    }
    return chunks
}

private fun splitLines(words: Iterable<Chunk>): List<Line> {
    val lines = mutableListOf<Line>()
    var line = mutableListOf<Span>()

    for (word in words) {
        if (word.text.endsWith("\n")) {
            lines += line
            line = mutableListOf()
        } else {
            line.add(Span.word(word.text, word.style))
        }
    }

    if (line.isNotEmpty()) lines += line

    return lines
}


private fun updateStyle(existingStyle: TextStyle, ansi: String): TextStyle {
    if (ansi.startsWith(OSC)) return existingStyle // TODO: OSC 8 (hyperlinks)
    if (!ansi.startsWith(CSI)) return existingStyle // DSC, APC, etc. don't affect style, discard them
    if (!ansi.endsWith("m")) return existingStyle // SGR sequences end in "m", others don't affect style

    // SGR sequences only contains numbers; anything else is malformed and we skip it.
    val codes = ansi.subSequence(2, ansi.length - 1)
            .split(";").mapNotNull { if (it.isEmpty()) 0 else it.toIntOrNull() }

    // Empty SGR is treated as reset
    if (codes.isEmpty()) return DEFAULT_STYLE

    var style = existingStyle
    var i = 0

    // https://en.wikipedia.org/wiki/ANSI_escape_code#SGR
    while (i <= codes.lastIndex) {
        when (val code = codes[i]) {
            // Resets
            AnsiStyle.reset.closeCode -> style = DEFAULT_STYLE
            AnsiStyle.bold.closeCode -> style = style.copy(bold = false)
            AnsiStyle.dim.closeCode -> style = style.copy(dim = false)
            AnsiStyle.italic.closeCode -> style = style.copy(italic = false)
            AnsiStyle.underline.closeCode -> style = style.copy(underline = false)
            AnsiStyle.inverse.closeCode -> style = style.copy(inverse = false)
            AnsiStyle.strikethrough.closeCode -> style = style.copy(strikethrough = false)
            AnsiCodes.fgColorReset -> style = style.copy(color = null)
            AnsiCodes.bgColorReset -> style = style.copy(bgColor = null)
            // Colors
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
            // Styles
            AnsiStyle.bold.openCode -> style = style.copy(bold = true)
            AnsiStyle.italic.openCode -> style = style.copy(italic = true)
            AnsiStyle.underline.openCode -> style = style.copy(underline = true)
            AnsiStyle.dim.openCode -> style = style.copy(dim = true)
            AnsiStyle.inverse.openCode -> style = style.copy(inverse = true)
            AnsiStyle.strikethrough.openCode -> style = style.copy(strikethrough = true)
            // Unsupported
            AnsiCodes.underlineColorSelector -> {
                // skip its arguments
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
