package com.github.ajalt.mordant.internal

import com.github.ajalt.colormath.Color
import com.github.ajalt.colormath.model.Ansi16
import com.github.ajalt.colormath.model.Ansi256
import com.github.ajalt.colormath.model.RGB
import com.github.ajalt.mordant.rendering.*

private const val NEL = '\u0085'
private const val LS = '\u2028'

/** Like a Span, but with no restrictions on [text] */
private data class Chunk(val text: String, val style: TextStyle)

/**
 * Split [text] into [Span]s, stripping ANSI codes and converting them to [TextStyle]s.
 *
 * Unknown ANSI codes are discarded.
 */
internal fun parseText(text: String, defaultStyle: TextStyle): Lines {
    val parseAnsi = parseAnsi(text, defaultStyle)
    val words = parseAnsi.flatMap { splitWords(it) }.toList()
    val splitLines = splitLines(words)
    return Lines(splitLines)
}

/** Split [text] into Chunks, splitting chunks only when the style changes */
private fun parseAnsi(text: String, defaultStyle: TextStyle): List<Chunk> {
    val commands = ANSI_RE.findAll(text).toList()
    if (commands.isEmpty()) return listOf(Chunk(text, defaultStyle))

    val parts = mutableListOf<Chunk>()
    var idxAfterLastCmd = 0
    var style = defaultStyle
    for (command in commands) {
        if (command.range.first > idxAfterLastCmd) {
            parts += Chunk(
                text = text.substring(idxAfterLastCmd, command.range.first),
                style = style
            )
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
/** Split a chunk into sequential whitespace/non-whitespace/linebreaks */
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

/** Split a flat list of chunks into a list of lines, removing linebreak chunks */
private fun splitLines(words: List<Chunk>): List<Line> {
    val lines = mutableListOf<Line>()
    var line = mutableListOf<Span>()

    for (word in words) {
        if (word.text.endsWith("\n")) {
            lines += Line(line, word.style)
            line = mutableListOf()
        } else {
            line.add(Span.word(word.text, word.style))
        }
    }

    if (line.isNotEmpty()) lines += Line(line)
    words.lastOrNull()?.let {
        if (it.text.endsWith("\n")) {
            lines.add(Line(listOf(), endStyle = it.style))
        }
    }

    return lines
}

/**
 * Return a copy of [existingStyle] updated with any supported styles from an [ansi] escape sequence.
 *
 * If the [ansi] contains any reset codes, the reset style attributes will be set to their
 * corresponding value from [defaultStyle].
 */
internal fun updateStyle(
    existingStyle: TextStyle,
    defaultStyle: TextStyle,
    ansi: String,
): TextStyle {
    if (ansi.startsWith(OSC)) return updateStyleWithOsc(ansi, existingStyle, defaultStyle)
    if (ansi.startsWith(CSI)) return updateStyleWithCsi(ansi, existingStyle, defaultStyle)
    return existingStyle // DSC, APC, etc. don't affect style, discard them
}

private fun updateStyleWithOsc(
    ansi: String,
    existingStyle: TextStyle,
    defaultStyle: TextStyle,
): TextStyle {
    if (!ansi.startsWith("${OSC}8")) return existingStyle // Only OSC 8 (hyperlinks) are supported
    val params = ansi.substring(3, ansi.length - 2).split(";")
    if (params.isEmpty()) return existingStyle // invalid ansi sequence
    val hyperlink = params.last().takeUnless { it.isBlank() }
    val id =
        if (hyperlink == null) defaultStyle.hyperlinkId else params.find { it.startsWith("id=") }
            ?.drop(3)
    return existingStyle.copy(
        hyperlink = hyperlink ?: defaultStyle.hyperlink,
        hyperlinkId = id
    )
}

private fun updateStyleWithCsi(
    ansi: String,
    existingStyle: TextStyle,
    defaultStyle: TextStyle,
): TextStyle {
    if (!ansi.endsWith("m")) return existingStyle // SGR sequences end in "m", others don't affect style

    // SGR sequences only contains numbers. Anything else is malformed, so we skip it.
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
                color = defaultStyle.color
                bgColor = defaultStyle.bgColor
                bold = defaultStyle.bold ?: false
                italic = defaultStyle.italic ?: false
                underline = defaultStyle.underline ?: false
                dim = defaultStyle.dim ?: false
                inverse = defaultStyle.inverse ?: false
                strikethrough = defaultStyle.strikethrough ?: false
            }

            AnsiCodes.boldAndDimClose -> {
                bold = defaultStyle.bold ?: false
                dim = defaultStyle.dim ?: false
            }

            AnsiCodes.italicClose -> italic = defaultStyle.italic ?: false
            AnsiCodes.underlineClose -> underline = defaultStyle.underline ?: false
            AnsiCodes.inverseClose -> inverse = defaultStyle.inverse ?: false
            AnsiCodes.strikethroughClose -> strikethrough = defaultStyle.strikethrough ?: false
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
                || codes[i + 3] !in 0..255
            ) {
                null to 0
            } else {
                RGB.from255(codes[i + 1], codes[i + 2], codes[i + 3]) to 4
            }
        }

        else -> null to 0
    }
}
