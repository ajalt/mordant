package com.github.ajalt.mordant.rendering

import com.github.ajalt.colormath.Color
import com.github.ajalt.colormath.model.*
import com.github.ajalt.mordant.internal.AnsiCodes
import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.HYPERLINK_RESET
import com.github.ajalt.mordant.rendering.AnsiLevel.*
import com.github.ajalt.mordant.terminal.Terminal

enum class AnsiLevel { NONE, ANSI16, ANSI256, TRUECOLOR }


/**
 * Default text style that can be applied to text.
 *
 * These styles are *not* automatically downsampled. You should print the styled strings with
 * [Terminal.println] to do so.
 *
 * ### Example
 *
 * ```
 * import com.github.ajalt.mordant.rendering.TextStyles.*
 *
 * val t = Terminal()
 * t.println("This text is ${bold("bold")}!")
 * ```
 */
// Unfortunately, this enum can't implement TextStyle because the enum values have the same name is
// TextStyle properties
@Suppress("EnumEntryName")
enum class TextStyles(val style: TextStyle) {
    bold(TextStyle(bold = true)),
    dim(TextStyle(dim = true)),
    italic(TextStyle(italic = true)),
    underline(TextStyle(underline = true)),
    inverse(TextStyle(inverse = true)),
    strikethrough(TextStyle(strikethrough = true)),

    /**
     * Reset the foreground color to the terminal default.
     *
     * ```
     * val style = (red on blue)
     * val backgroundOnly = style + resetForeground
     * ```
     */
    resetForeground(TextStyle(Ansi16(AnsiCodes.fgColorReset))),

    /**
     * Reset the background color to the terminal default.
     *
     * ```
     * val style = (red on blue)
     * val foregroundOnly = style + resetBackground
     * ```
     */
    resetBackground(TextStyle(bgColor = Ansi16(AnsiCodes.bgColorReset))),

    /**
     * Reset all styles to the terminal default.
     *
     * ```
     * val style = (red on blue) + bold
     * assert reset(style("text")) == "text"
     * ```
     */
    reset(
        TextStyle(
            color = Ansi16(AnsiCodes.fgColorReset),
            bgColor = Ansi16(AnsiCodes.bgColorReset),
            bold = false,
            italic = false,
            underline = false,
            dim = false,
            inverse = false,
            strikethrough = false,
            hyperlink = HYPERLINK_RESET,
        )
    ),

    ;

    operator fun invoke(text: String) = style.invoke(text)
    operator fun plus(other: TextStyle) = style + other
    operator fun plus(other: TextStyles) = style + other.style

    companion object {
        /**
         * Create a text style with a hyperlink.
         *
         * The [destination] should include an explicit protocol like `https://`, since most
         * terminals won't open links without one.
         */
        fun hyperlink(destination: String): TextStyle = TextStyle(hyperlink = destination)
    }

    override fun toString() = style.toString()
}

/**
 * Default text colors that can be used to style text.
 *
 * These styles are *not* automatically downsampled. You should print the styled strings with
 * [Terminal.println] to do so.
 *
 * ### Example
 *
 * ```
 * import com.github.ajalt.mordant.rendering.TextColors.*
 *
 * val t = Terminal()
 * t.println("This text is ${green("colorful")}!")
 * ```
 */
@Suppress("EnumEntryName")
enum class TextColors(
    private val textStyle: TextStyle,
) : Color by textStyle.color!!, TextStyle by textStyle {
    black(TextStyle(Ansi16(30))),
    red(TextStyle(Ansi16(31))),
    green(TextStyle(Ansi16(32))),
    yellow(TextStyle(Ansi16(33))),
    blue(TextStyle(Ansi16(34))),
    magenta(TextStyle(Ansi16(35))),
    cyan(TextStyle(Ansi16(36))),
    white(TextStyle(Ansi16(37))),
    gray(TextStyle(Ansi16(90))),

    brightRed(TextStyle(Ansi16(91))),
    brightGreen(TextStyle(Ansi16(92))),
    brightYellow(TextStyle(Ansi16(93))),
    brightBlue(TextStyle(Ansi16(94))),
    brightMagenta(TextStyle(Ansi16(95))),
    brightCyan(TextStyle(Ansi16(96))),
    brightWhite(TextStyle(Ansi16(97)));

    override fun toString() = textStyle.toString()

    companion object {
        /** @param hex An rgb hex string in the form "#ffffff" or "ffffff" */
        fun rgb(hex: String, level: AnsiLevel = TRUECOLOR): TextStyle = color(RGB(hex), level)

        /**
         * Create a color code from an RGB color.
         *
         * @param r The red amount, in the range `[0, 1]`
         * @param g The green amount, in the range `[0, 1]`
         * @param b The blue amount, in the range `[0, 1]`
         */
        fun rgb(r: Number, g: Number, b: Number, level: AnsiLevel = TRUECOLOR): TextStyle {
            require(r.toFloat() in 0f..1f) { "r must be a number in the range [0, 1]" }
            require(g.toFloat() in 0f..1f) { "g must be a number in the range [0, 1]" }
            require(b.toFloat() in 0f..1f) { "b must be a number in the range [0, 1]" }

            return color(RGB(r, g, b), level)
        }

        /**
         * Create a color code from an HSL color.
         *
         * @param h The hue, in the range `[0, 360]`
         * @param s The saturation, in the range `[0, 1]`
         * @param l The lightness, in the range `[0, 1]`
         */
        fun hsl(h: Number, s: Number, l: Number, level: AnsiLevel = TRUECOLOR): TextStyle {
            require(s.toFloat() in 0f..1f) { "saturation must be a number in the range [0, 1]" }
            require(l.toFloat() in 0f..1f) { "lightness must be a number in the range [0, 1]" }
            return color(HSL(h, s, l), level)
        }

        /**
         * Create a color code from an HSV color.
         *
         * @param h The hue, in the range `[0, 360]`
         * @param s The saturation, in the range `[0, 1]`
         * @param v The value, in the range `[0, 1]`
         */
        fun hsv(h: Number, s: Number, v: Number, level: AnsiLevel = TRUECOLOR): TextStyle {
            require(s.toFloat() in 0f..1f) { "saturation must be a number in the range `[0, 1]`" }
            require(v.toFloat() in 0f..1f) { "value must be a number in the range `[0, 1]`" }
            return color(HSV(h, s, v), level)
        }

        /**
         * Create a color code from a CMYK color.
         *
         * @param c The cyan amount, in the range `[0, 100]`
         * @param m The magenta amount, in the range `[0, 100]`
         * @param y The yellow amount, in the range `[0, 100]`
         * @param k The black amount, in the range `[0, 100]`
         */
        fun cmyk(c: Int, m: Int, y: Int, k: Int, level: AnsiLevel = TRUECOLOR): TextStyle =
            color(CMYK(c, m, y, k), level)

        /**
         * Create a grayscale color code from a fraction in the range \[0, 1].
         *
         * @param fraction The fraction of white in the color. 0 is pure black, 1 is pure white.
         */
        fun gray(fraction: Number, level: AnsiLevel = TRUECOLOR): TextStyle {
            require(fraction.toFloat() in 0f..1f) { "fraction must be in the range [0, 1]" }
            return rgb(fraction, fraction, fraction, level)
        }

        /**
         * Create a color code from a CIE XYZ color.
         *
         * Conversions use D65 reference white, and sRGB profile.
         *
         * [x], [y], and [z] are generally in the interval `[0, 1]`
         */
        fun xyz(x: Number, y: Number, z: Number, level: AnsiLevel = TRUECOLOR): TextStyle =
            color(XYZ(x, y, z), level)


        /**
         * Create a color code from a CIE LAB color.
         *
         * Conversions use D65 reference white, and sRGB profile.
         *
         * [l] is in the interval `[0, 100]`. [a] and [b] have unlimited range,
         * but are generally in `[-100, 100]`
         */
        fun lab(l: Number, a: Number, b: Number, level: AnsiLevel = TRUECOLOR): TextStyle =
            color(LAB(l, a, b), level)


        /**
         * Create a [TextStyle] with a foreground of [color], downsampled to a given [level].
         *
         * It's usually easier to use a function like [rgb] or [hsl] instead.
         */
        fun color(color: Color, level: AnsiLevel = TRUECOLOR): TextStyle {
            val c = when (color) {
                is TextStyle -> color.color ?: return DEFAULT_STYLE
                else -> color
            }
            return TextStyle(
                when (level) {
                    NONE -> null
                    ANSI16 -> c.toAnsi16()
                    ANSI256 -> if (c is Ansi16) c else c.toAnsi256()
                    TRUECOLOR -> c
                },
            )
        }
    }
}
