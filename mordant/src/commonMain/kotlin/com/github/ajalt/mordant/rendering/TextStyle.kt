package com.github.ajalt.mordant.rendering

import com.github.ajalt.colormath.Color
import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.generateHyperlinkId
import com.github.ajalt.mordant.internal.invokeStyle

/**
 * Styles that can be applied to text by terminals that support it.
 *
 * You can combine styles with [on] or [plus]
 */
interface TextStyle {
    val color: Color?
    val bgColor: Color?
    val bold: Boolean?
    val italic: Boolean?
    val underline: Boolean?
    val dim: Boolean?
    val inverse: Boolean?
    val strikethrough: Boolean?
    val hyperlink: String?
    val hyperlinkId: String?

    /**
     * Create a with this [color] as the [background][bgColor].
     *
     * ### Example
     * ```
     * red.bg == TextStyle(bgColor=red)
     * ```
     */
    val bg: TextStyle

    /**
     * Create a style with the foreground color of [bg] as the background color of the new style.
     *
     * All other attributes will be copied from this style.
     *
     * ### Example
     * ```
     * (red on blue) == TextStyle(color=red, bgColor=blue)
     *
     * val style1 = TextStyle(red, bold=true)
     * val style2 = TextStyle(blue, italic=true)
     * (style1 on style2) == TextStyle(red, blue, bold=true)
     * ```
     */
    infix fun on(bg: TextStyle): TextStyle

    /**
     * Apply this style to [text].
     */
    operator fun invoke(text: String): String = invokeStyle(text)


    /**
     * Return a new style that combines this style with [other].
     *
     * And attributes that are set on both this and [other] will take the value from [other]. Any
     * `null` values on [other] will keep the value from this style.
     *
     * ### Example
     * ```
     * val style1 = TextStyle(red, blue, bold=true, italic=true)
     * val style2 = TextStyle(green, italic=false, dim=true)
     * (style1 + style2) == TextStyle(green, blue, bold=true, italic=false, dim=true)
     * ```
     */
    operator fun plus(other: TextStyle): TextStyle {
        return when {
            this === DEFAULT_STYLE -> other
            other === DEFAULT_STYLE -> this
            else -> TxtStyle(
                color = other.color ?: color,
                bgColor = other.bgColor ?: bgColor,
                bold = other.bold ?: bold,
                italic = other.italic ?: italic,
                underline = other.underline ?: underline,
                dim = other.dim ?: dim,
                inverse = other.inverse ?: inverse,
                strikethrough = other.strikethrough ?: strikethrough,
                hyperlink = other.hyperlink ?: hyperlink,
                hyperlinkId = other.hyperlinkId ?: hyperlinkId
            )
        }
    }

    operator fun plus(other: TextStyles): TextStyle = this + other.style
}

fun TextStyle(
    color: Color? = null,
    bgColor: Color? = null,
    bold: Boolean? = null,
    italic: Boolean? = null,
    underline: Boolean? = null,
    dim: Boolean? = null,
    inverse: Boolean? = null,
    strikethrough: Boolean? = null,
    hyperlink: String? = null,
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
    override val bold: Boolean?,
    override val italic: Boolean?,
    override val underline: Boolean?,
    override val dim: Boolean?,
    override val inverse: Boolean?,
    override val strikethrough: Boolean?,
    override val hyperlink: String?,
    override val hyperlinkId: String?,
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
    hyperlinkId: String? = this.hyperlinkId,
) = TxtStyle(
    fg, bg, bold, italic, underline, dim, inverse, strikethrough, hyperlink, hyperlinkId
)

internal fun foldStyles(vararg styles: TextStyle?): TextStyle? {
    var style: TextStyle? = null
    for (s in styles) {
        if (s == null) continue
        style = when (style) {
            null -> s
            else -> s + style
        }
    }
    return style
}
