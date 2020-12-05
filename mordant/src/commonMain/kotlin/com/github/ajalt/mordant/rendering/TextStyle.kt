package com.github.ajalt.mordant.rendering

import com.github.ajalt.colormath.Color
import com.github.ajalt.mordant.internal.generateHyperlinkId
import com.github.ajalt.mordant.internal.invokeStyle
import com.github.ajalt.mordant.terminal.TextStyles

internal val DEFAULT_STYLE = TextStyle()

interface TextStyle {
    val color: Color?
    val bgColor: Color?
    val bold: Boolean
    val italic: Boolean
    val underline: Boolean
    val dim: Boolean
    val inverse: Boolean
    val strikethrough: Boolean
    val hyperlink: String?
    val hyperlinkId: String?

    val bg: TextStyle

    infix fun on(bg: TextStyle): TextStyle

    operator fun invoke(text: String): String = invokeStyle(text)

    operator fun plus(other: TextStyles): TextStyle = this + other.style
    operator fun plus(other: TextStyle): TextStyle {
        return when {
            this === DEFAULT_STYLE -> other
            other === DEFAULT_STYLE -> this
            else -> TxtStyle(
                    color = other.color ?: color,
                    bgColor = other.bgColor ?: bgColor,
                    bold = other.bold || bold,
                    italic = other.italic || italic,
                    underline = other.underline || underline,
                    dim = other.dim || dim,
                    inverse = other.inverse || inverse,
                    strikethrough = other.strikethrough || strikethrough,
                    hyperlink = other.hyperlink ?: hyperlink,
                    hyperlinkId = other.hyperlinkId ?: hyperlinkId
            )
        }
    }
}

@Suppress("FunctionName")
fun TextStyle(
        color: Color? = null,
        bgColor: Color? = null,
        bold: Boolean = false,
        italic: Boolean = false,
        underline: Boolean = false,
        dim: Boolean = false,
        inverse: Boolean = false,
        strikethrough: Boolean = false,
        hyperlink: String? = null
): TextStyle = TxtStyle(
        color = color,
        bgColor = bgColor,
        bold = bold,
        italic = italic,
        underline = underline,
        dim = dim,
        inverse = inverse,
        strikethrough = strikethrough,
        hyperlink = hyperlink,
        hyperlinkId = hyperlink?.let { generateHyperlinkId() }
)

internal data class TxtStyle(
        override val color: Color?,
        override val bgColor: Color?,
        override val bold: Boolean,
        override val italic: Boolean,
        override val underline: Boolean,
        override val dim: Boolean,
        override val inverse: Boolean,
        override val strikethrough: Boolean,
        override val hyperlink: String?,
        override val hyperlinkId: String?
) : TextStyle {
    override infix fun on(bg: TextStyle): TextStyle {
        return copy(bgColor = bg.color)
    }

    override val bg: TextStyle get() = copy(color = null, bgColor = color)
}

internal fun TextStyle.copy(
        fg: Color? = color,
        bg: Color? = bgColor,
        hyperlink: String? = this.hyperlink,
        hyperlinkId: String? = this.hyperlinkId
) = TxtStyle(
        fg, bg, bold, italic, underline, dim, inverse, strikethrough, hyperlink, hyperlinkId
)

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
