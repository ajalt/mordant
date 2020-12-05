package com.github.ajalt.mordant.internal

import com.github.ajalt.colormath.Ansi16
import com.github.ajalt.colormath.Ansi256
import com.github.ajalt.colormath.Color
import com.github.ajalt.colormath.RGB
import com.github.ajalt.mordant.rendering.*

internal val ANSI_RE = Regex("""$OSC[^$ESC]*$ESC\\|$ESC(?:[@-Z\\-_]|\[[0-?]*[ -/]*[@-~])""")
private const val NEL = '\u0085'
private const val LS = '\u2028'

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
        style = updateStyle(style, defaultStyle, command.value)
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
    var chunkType = -1 // 0=newline, 1=always break chunk, 2=space, 3=word
    val t = chunk.text
    while (i < t.length) {
        val c = t[i]
        val type = when {
            c == '\r' -> 0
            c == '\n' || c == '\t' || c == NEL || c == LS -> 1
            c.isWhitespace() -> 2
            else -> 3
        }
        if (i == 0) {
            chunkType = type
        } else if (type == 1 || chunkType != type) {
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

/**
 * Return a copy of [existingStyle] updated with any supported styles from an [ansi] escape sequence.
 *
 * If the [ansi] contains any reset codes, the reset style attributes will be set to their
 * corresponding value from [defaultStyle].
 */
internal fun updateStyle(existingStyle: TextStyle, defaultStyle: TextStyle, ansi: String): TextStyle {
    if (ansi.startsWith(OSC)) return updateStyleWithOsc(ansi, existingStyle, defaultStyle)
    if (ansi.startsWith(CSI)) return updateStyleWithCsi(ansi, existingStyle, defaultStyle)
    return existingStyle // DSC, APC, etc. don't affect style, discard them
}

private fun updateStyleWithOsc(ansi: String, existingStyle: TextStyle, defaultStyle: TextStyle): TextStyle {
    if (!ansi.startsWith("${OSC}8")) return existingStyle // Only OSC 8 (hyperlinks) are supported
    val params = ansi.substring(3, ansi.length - 2).split(";")
    if (params.isEmpty()) return existingStyle // invalid ansi sequence
    val hyperlink = params.last().takeUnless { it.isBlank() }
    val id = if (hyperlink == null) defaultStyle.hyperlinkId else params.find { it.startsWith("id=") }?.drop(3)
    return existingStyle.copy(
            hyperlink = hyperlink ?: defaultStyle.hyperlink,
            hyperlinkId = id
    )
}

private fun updateStyleWithCsi(ansi: String, existingStyle: TextStyle, defaultStyle: TextStyle): TextStyle {
    if (!ansi.endsWith("m")) return existingStyle // SGR sequences end in "m", others don't affect style

    // SGR sequences only contains numbers; anything else is malformed and we skip it.
    val codes = ansi.subSequence(2, ansi.length - 1)
            .split(";").mapNotNull { if (it.isEmpty()) 0 else it.toIntOrNull() }

    // Empty SGR is treated as reset
    if (codes.isEmpty()) return defaultStyle

    var color = existingStyle.color
    var bgColor = existingStyle.bgColor
    var bold = existingStyle.bold
    var italic = existingStyle.italic
    var underline = existingStyle.underline
    var dim = existingStyle.dim
    var inverse = existingStyle.inverse
    var strikethrough = existingStyle.strikethrough

    var i = 0

    // https://en.wikipedia.org/wiki/ANSI_escape_code#SGR
    while (i <= codes.lastIndex) {
        when (val code = codes[i]) {
            // Resets
            AnsiCodes.reset -> {
                color = existingStyle.color
                bgColor = existingStyle.bgColor
                bold = existingStyle.bold
                italic = existingStyle.italic
                underline = existingStyle.underline
                dim = existingStyle.dim
                inverse = existingStyle.inverse
                strikethrough = existingStyle.strikethrough
            }
            AnsiCodes.boldAndDimClose -> {
                bold = defaultStyle.bold
                dim = defaultStyle.dim
            }
            AnsiCodes.italicClose -> italic = defaultStyle.italic
            AnsiCodes.underlineClose -> underline = defaultStyle.underline
            AnsiCodes.inverseClose -> inverse = defaultStyle.inverse
            AnsiCodes.strikethroughClose -> strikethrough = defaultStyle.strikethrough
            AnsiCodes.fgColorReset -> color = defaultStyle.color
            AnsiCodes.bgColorReset -> bgColor = defaultStyle.bgColor
            // Colors
            in AnsiCodes.fg16Range, in AnsiCodes.fg16BrightRange -> {
                color = Ansi16(code)
            }
            in AnsiCodes.bg16Range, in AnsiCodes.bg16BrightRange -> {
                bgColor = Ansi16(code - AnsiCodes.fgBgOffset)
            }
            AnsiCodes.fgColorSelector -> {
                val (c, consumed) = getAnsiColor(i + 1, codes)
                if (c == null) break // Unrecognized code format: stop parsing
                color = c
                i += consumed
            }
            AnsiCodes.bgColorSelector -> {
                val (c, consumed) = getAnsiColor(i + 1, codes)
                if (c == null) break
                bgColor = c
                i += consumed
            }
            // Styles
            AnsiCodes.boldOpen -> bold = true
            AnsiCodes.italicOpen -> italic = true
            AnsiCodes.underlineOpen -> underline = true
            AnsiCodes.dimOpen -> dim = true
            AnsiCodes.inverseOpen -> inverse = true
            AnsiCodes.strikethroughOpen -> strikethrough = true
            // Unsupported
            AnsiCodes.underlineColorSelector -> {
                // skip its arguments
                i += when {
                    i == codes.lastIndex -> 0
                    codes.getOrNull(i + 1) == AnsiCodes.selector256 -> 1
                    codes.getOrNull(i + 1) == AnsiCodes.selectorRgb -> 3
                    else -> break
                }
            }
        }
        i += 1
    }

    return TxtStyle(
            color = color,
            bgColor = bgColor,
            bold = bold,
            italic = italic,
            underline = underline,
            dim = dim,
            inverse = inverse,
            strikethrough = strikethrough,
            hyperlink = existingStyle.hyperlink,
            hyperlinkId = existingStyle.hyperlinkId
    )
}

private fun getAnsiColor(i: Int, codes: List<Int>): Pair<Color?, Int> {
    return when (codes[i]) {
        AnsiCodes.selector256 -> {
            if (i + 1 > codes.lastIndex
                    || codes[i + 1] !in 0..255
            ) {
                null to 0
            } else {
                Ansi256(codes[i + 1]) to 2
            }
        }
        AnsiCodes.selectorRgb -> {
            if (i + 3 > codes.lastIndex
                    || codes[i + 1] !in 0..255
                    || codes[i + 2] !in 0..255
                    || codes[i + 3] !in 0..255) {
                null to 0
            } else {
                RGB(codes[i + 1], codes[i + 2], codes[i + 3]) to 4
            }
        }
        else -> null to 0
    }
}
