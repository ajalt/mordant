package com.github.ajalt.mordant.rendering

import com.github.ajalt.colormath.ConvertibleColor
import com.github.ajalt.mordant.TextStyleContainer
import com.github.ajalt.mordant.rendering.internal.invokeStyle

internal val DEFAULT_STYLE = TextStyle()

interface TextStyle {
    val color: ConvertibleColor?
    val bgColor: ConvertibleColor?
    val bold: Boolean
    val italic: Boolean
    val underline: Boolean
    val dim: Boolean
    val inverse: Boolean
    val strikethrough: Boolean

    val bg: TextStyle

    infix fun on(bg: TextStyle): TextStyle
    infix fun on(bg: ConvertibleColor): TextStyle

    operator fun invoke(text: String): String = invokeStyle(text)

    operator fun plus(other: TextStyleContainer): TextStyle = this + other.style
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

@Suppress("FunctionName")
fun TextStyle(
        color: ConvertibleColor? = null,
        bgColor: ConvertibleColor? = null,
        bold: Boolean = false,
        italic: Boolean = false,
        underline: Boolean = false,
        dim: Boolean = false,
        inverse: Boolean = false,
        strikethrough: Boolean = false
): TextStyle = TextStyleImpl(
        color = color,
        bgColor = bgColor,
        bold = bold,
        italic = italic,
        underline = underline,
        dim = dim,
        inverse = inverse,
        strikethrough = strikethrough,
)

private data class TextStyleImpl(
        override val color: ConvertibleColor?,
        override val bgColor: ConvertibleColor?,
        override val bold: Boolean,
        override val italic: Boolean,
        override val underline: Boolean,
        override val dim: Boolean,
        override val inverse: Boolean,
        override val strikethrough: Boolean
) : TextStyle {
    override infix fun on(bg: TextStyle): TextStyle {
        return copy(bgColor = bg.color)
    }

    override infix fun on(bg: ConvertibleColor): TextStyle {
        return copy(bgColor = bg)
    }

    override val bg: TextStyle get() = copy(color = null, bgColor = color)
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
