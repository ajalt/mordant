package com.github.ajalt.mordant.rendering

import com.github.ajalt.colormath.ConvertibleColor
import com.github.ajalt.mordant.AnsiCode
import com.github.ajalt.mordant.Terminal

internal val DEFAULT_STYLE = TextStyle()

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
    operator fun plus(other: TextStyle): TextStyle {
        return when {
            this === DEFAULT_STYLE -> other
            other === DEFAULT_STYLE -> this
            else -> TextStyle(
                    color = other.color ?: color,
                    bgColor = other.bgColor ?: bgColor,
                    bold = other.bold || bold,
                    italic = other.italic || italic,
                    underline = other.underline || underline,
                    dim = other.dim || dim,
                    inverse = other.inverse || inverse,
                    strikethrough = other.strikethrough || strikethrough,
            )
        }
    }
}


internal fun foldStyles(vararg styles: TextStyle?): TextStyle? {
    var style: TextStyle? = null
    for (s in styles) {
        if (s == null) continue
        style = when (style) {
            null -> s
            else -> style + s
        }
    }
    return style
}
