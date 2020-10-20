package com.github.ajalt.mordant.rendering.internal

import com.github.ajalt.colormath.Ansi16
import com.github.ajalt.colormath.Ansi256
import com.github.ajalt.colormath.Color
import com.github.ajalt.mordant.AnsiLevel
import com.github.ajalt.mordant.TextColorContainer
import com.github.ajalt.mordant.rendering.DEFAULT_STYLE
import com.github.ajalt.mordant.rendering.Lines
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.internal.AnsiCodes.bgColorReset
import com.github.ajalt.mordant.rendering.internal.AnsiCodes.bgColorSelector
import com.github.ajalt.mordant.rendering.internal.AnsiCodes.fgBgOffset
import com.github.ajalt.mordant.rendering.internal.AnsiCodes.fgColorReset
import com.github.ajalt.mordant.rendering.internal.AnsiCodes.fgColorSelector
import com.github.ajalt.mordant.rendering.internal.AnsiCodes.selector256
import com.github.ajalt.mordant.rendering.internal.AnsiCodes.selectorRgb

private val ANSI_CSI_RE = Regex("""$ESC\[[\d;]*m""")

internal fun renderLinesAnsi(lines: Lines, level: AnsiLevel): String = buildString {
    for ((i, line) in lines.lines.withIndex()) {
        if (i > 0) append("\n")

        // Concat equal ansi codes to avoid closing and reopening them on every span
        var activeStyle: TextStyle = DEFAULT_STYLE
        for (span in line) {
            val newStyle = downsample(span.style, level)
            append(makeTag(activeStyle, newStyle))
            activeStyle = newStyle
            append(span.text)
        }
        append(makeTag(activeStyle, DEFAULT_STYLE))
    }
}

// For applying a single style to a string, we use a regex replace rather than a full parse and
// render, since parsing is a little slower due to the need to split on whitespace.
internal fun TextStyle.invokeStyle(text: String): String {
    if (text.isEmpty()) return ""
    var openStyle = this
    var style = this
    val inner = ANSI_CSI_RE.replace(text) { match ->
        if (match.range.last == text.lastIndex) return@replace ""
        val new = updateStyle(style, this, match.value)
        if (match.range.first == 0) {
            openStyle = new
            style = new
            return@replace ""
        }
        val tag = makeTag(style, new)
        style = new
        tag
    }
    return "${makeTag(DEFAULT_STYLE, openStyle)}$inner${makeTag(style, DEFAULT_STYLE)}"
}

private fun downsample(style: TextStyle, level: AnsiLevel): TextStyle {
    return if (style === DEFAULT_STYLE) style else when (level) {
        AnsiLevel.NONE -> DEFAULT_STYLE
        AnsiLevel.ANSI16 -> style.copy(
                fg = style.color?.toAnsi16(),
                bg = style.bgColor?.toAnsi16()
        )
        AnsiLevel.ANSI256 -> style.copy(
                fg = style.color?.let { if (it is Ansi16) it else it.toAnsi256() },
                bg = style.bgColor?.let { if (it is Ansi16) it else it.toAnsi256() }
        )
        AnsiLevel.TRUECOLOR -> style
    }
}

private fun TextStyle.copy(fg: Color?, bg: Color?) = TextStyle(
        fg, bg, bold, italic, underline, dim, inverse, strikethrough
)

private fun makeTag(old: TextStyle, new: TextStyle): String {
    if (old == new) return ""
    val codes = mutableListOf<Int>()
    if (old.color != new.color) codes += new.color.toAnsi(fgColorSelector, fgColorReset, 0)
    if (old.bgColor != new.bgColor) codes += new.bgColor.toAnsi(bgColorSelector, bgColorReset, fgBgOffset)

    fun style(old: Boolean, new: Boolean, open: Int, close: Int) {
        if (old != new) codes += if (new) open else close
    }

    style(old.bold, new.bold, AnsiCodes.boldOpen, AnsiCodes.boldAndDimClose)
    style(old.italic, new.italic, AnsiCodes.italicOpen, AnsiCodes.italicClose)
    style(old.underline, new.underline, AnsiCodes.underlineOpen, AnsiCodes.underlineClose)
    style(old.dim, new.dim, AnsiCodes.dimOpen, AnsiCodes.boldAndDimClose)
    style(old.inverse, new.inverse, AnsiCodes.inverseOpen, AnsiCodes.inverseClose)
    style(old.strikethrough, new.strikethrough, AnsiCodes.strikethroughOpen, AnsiCodes.strikethroughClose)

    return if (codes.isEmpty()) "" else codes.joinToString(";", prefix = CSI, postfix = "m")
}

private fun Color?.toAnsi(select: Int, reset: Int, offset: Int): List<Int> {
    return when (val it = (this as? TextColorContainer)?.style?.color ?: (this)) {
        null -> listOf(reset)
        is Ansi16 -> listOf(it.code + offset)
        is Ansi256 -> listOf(select, selector256, it.code)
        else -> it.toRGB().run { listOf(select, selectorRgb, r, g, b) }
    }
}
