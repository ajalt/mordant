package com.github.ajalt.mordant.terminal

import com.github.ajalt.colormath.*
import com.github.ajalt.mordant.rendering.DEFAULT_STYLE
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.terminal.AnsiLevel.*
import kotlin.math.roundToInt

enum class AnsiLevel { NONE, ANSI16, ANSI256, TRUECOLOR }

// Unfortunately, this enum can't implement TextStyle because the enum values have the same name is
// TextStyle properties
@Suppress("EnumEntryName")
enum class TextStyles(val style: TextStyle) {
    bold(TextStyle(bold = true)),
    dim(TextStyle(dim = true)),
    italic(TextStyle(italic = true)),
    underline(TextStyle(underline = true)),
    inverse(TextStyle(inverse = true)),
    strikethrough(TextStyle(strikethrough = true));

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
         * @param r The red amount, in the range \[0, 255]
         * @param g The green amount, in the range \[0, 255]
         * @param b The blue amount, in the range \[0, 255]
         */
        fun rgb(r: Int, g: Int, b: Int, level: AnsiLevel = TRUECOLOR): TextStyle = color(RGB(r, g, b), level)

        /**
         * Create a color code from an HSL color.
         *
         * @param h The hue, in the range \[0, 360]
         * @param s The saturation, in the range \[0, 100]
         * @param l The lightness, in the range \[0, 100]
         */
        fun hsl(h: Int, s: Int, l: Int, level: AnsiLevel = TRUECOLOR): TextStyle = color(HSL(h, s, l), level)

        /**
         * Create a color code from an HSV color.
         *
         * @param h The hue, in the range \[0, 360]
         * @param s The saturation, in the range \[0,100]
         * @param v The value, in the range \[0,100]
         */
        fun hsv(h: Int, s: Int, v: Int, level: AnsiLevel = TRUECOLOR): TextStyle = color(HSV(h, s, v), level)

        /**
         * Create a color code from a CMYK color.
         *
         * @param c The cyan amount, in the range \[0, 100]
         * @param m The magenta amount, in the range \[0,100]
         * @param y The yellow amount, in the range \[0,100]
         * @param k The black amount, in the range \[0,100]
         */
        fun cmyk(c: Int, m: Int, y: Int, k: Int, level: AnsiLevel = TRUECOLOR): TextStyle = color(CMYK(c, m, y, k), level)

        /**
         * Create a grayscale color code from a fraction in the range \[0, 1].
         *
         * @param fraction The fraction of white in the color. 0 is pure black, 1 is pure white.
         */
        fun gray(fraction: Double, level: AnsiLevel = TRUECOLOR): TextStyle {
            require(fraction in 0.0..1.0) { "fraction must be in the range [0, 1]" }
            return (255 * fraction).roundToInt().let { rgb(it, it, it, level) }
        }

        /**
         * Create a color code from a CIE XYZ color.
         *
         * Conversions use D65 reference white, and sRGB profile.
         *
         * [x], [y], and [z] are generally in the interval [0, 100], but may be larger
         */
        fun xyz(x: Double, y: Double, z: Double, level: AnsiLevel = TRUECOLOR): TextStyle = color(XYZ(x, y, z), level)


        /**
         * Create a color code from a CIE LAB color.
         *
         * Conversions use D65 reference white, and sRGB profile.
         *
         * [l] is in the interval [0, 100]. [a] and [b] have unlimited range,
         * but are generally in [-100, 100]
         */
        fun lab(l: Double, a: Double, b: Double, level: AnsiLevel = TRUECOLOR): TextStyle = color(LAB(l, a, b), level)


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
