package com.github.ajalt.mordant.internal

import com.github.ajalt.colormath.Ansi16
import com.github.ajalt.colormath.Ansi256
import com.github.ajalt.colormath.Color
import com.github.ajalt.mordant.terminal.AnsiLevel
import com.github.ajalt.mordant.rendering.DEFAULT_STYLE
import com.github.ajalt.mordant.rendering.Lines
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.copy
import com.github.ajalt.mordant.internal.AnsiCodes.bgColorReset
import com.github.ajalt.mordant.internal.AnsiCodes.bgColorSelector
import com.github.ajalt.mordant.internal.AnsiCodes.fgBgOffset
import com.github.ajalt.mordant.internal.AnsiCodes.fgColorReset
import com.github.ajalt.mordant.internal.AnsiCodes.fgColorSelector
import com.github.ajalt.mordant.internal.AnsiCodes.selector256
import com.github.ajalt.mordant.internal.AnsiCodes.selectorRgb

internal fun renderLinesAnsi(lines: Lines, level: AnsiLevel, hyperlinks: Boolean): String = buildString {
    for ((i, line) in lines.lines.withIndex()) {
        if (i > 0) append("\n")

        // Concat equal ansi codes to avoid closing and reopening them on every span
        var activeStyle: TextStyle = DEFAULT_STYLE
        for (span in line) {
            val newStyle = downsample(span.style, level, hyperlinks)
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
    val inner = ANSI_RE.replace(text) { match ->
        // Remove any tags at the beginning or end of `text`, since we always make new ones
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

private fun downsample(style: TextStyle, level: AnsiLevel, hyperlinks: Boolean): TextStyle {
    return if (style === DEFAULT_STYLE) style else when (level) {
        AnsiLevel.NONE -> DEFAULT_STYLE
        AnsiLevel.ANSI16 -> style.copy(
                fg = style.color?.toAnsi16(),
                bg = style.bgColor?.toAnsi16(),
                hyperlink = style.hyperlink.takeIf { hyperlinks },
                hyperlinkId = style.hyperlinkId.takeIf { hyperlinks }
        )
        AnsiLevel.ANSI256 -> style.copy(
                fg = style.color?.let { if (it is Ansi16) it else it.toAnsi256() },
                bg = style.bgColor?.let { if (it is Ansi16) it else it.toAnsi256() },
                hyperlink = style.hyperlink.takeIf { hyperlinks },
                hyperlinkId = style.hyperlinkId.takeIf { hyperlinks }
        )
        AnsiLevel.TRUECOLOR -> if (hyperlinks || style.hyperlink == null) style else style.copy(
                fg = style.color,
                bg = style.bgColor,
                hyperlink = null,
                hyperlinkId = null
        )
    }
}


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

    val csi = if (codes.isEmpty()) "" else codes.joinToString(";", prefix = CSI, postfix = "m")
    return when {
        old.hyperlink != new.hyperlink -> csi + makeHyperlinkTag(new.hyperlink, new.hyperlinkId)
        else -> csi
    }
}

private fun makeHyperlinkTag(hyperlink: String?, hyperlinkId: String?): String {
    if (hyperlink == null) return "${OSC}8;;$ST"
    val id = hyperlinkId?.let { "id=$it" } ?: ""
    return "${OSC}8;$id;$hyperlink$ST"
}

private fun Color?.toAnsi(select: Int, reset: Int, offset: Int): List<Int> {
    return when (val it = (this as? TextStyle)?.color ?: this) {
        null -> listOf(reset)
        is Ansi16 -> listOf(it.code + offset)
        is Ansi256 -> listOf(select, selector256, it.code)
        // The ITU T.416 spec uses colons for the rgb separator as well as extra parameters for CMYK
        // and such. Most terminals only support the semicolon form, so that's what we use.
        // https://gist.github.com/XVilka/8346728#gistcomment-2774523
        else -> it.toRGB().run { listOf(select, selectorRgb, r, g, b) }
    }
}
