package com.github.ajalt.mordant.rendering

import com.github.ajalt.colormath.ConvertibleColor
import com.github.ajalt.mordant.AnsiCode
import com.github.ajalt.mordant.Terminal

data class TextStyle(
        val color: ConvertibleColor? = null,
        val bgColor: ConvertibleColor? = null,
        val bold: Boolean = false,
        val italic: Boolean = false,
        val underline: Boolean = false,
        val dim: Boolean = false,
        val inverse: Boolean = false,
        val strikethrough: Boolean = false
) {
    operator fun plus(other: TextStyle) = TextStyle(
            color = other.color ?: color,
            bgColor = other.bgColor ?: bgColor,
            bold = bold || other.bold,
            italic = italic || other.italic,
            underline = underline || other.underline,
            dim = dim || other.dim,
            inverse = inverse || other.inverse,
            strikethrough = strikethrough || other.strikethrough,
    )
}

internal fun TextStyle.toAnsi(t: Terminal): AnsiCode {
    var code = color?.let { t.colors.color(it) } ?: t.colors.plain

    if (bgColor != null) code += t.colors.color(bgColor).bg
    if (bold) code += t.colors.bold
    if (italic) code += t.colors.italic
    if (underline) code += t.colors.underline
    if (dim) code += t.colors.dim
    if (inverse) code += t.colors.inverse
    if (strikethrough) code += t.colors.strikethrough

    return code
}

internal val DEFAULT_STYLE = TextStyle()
