package com.github.ajalt.mordant

import com.github.ajalt.colormath.ConvertibleColor
import com.github.ajalt.colormath.RGB
import kotlin.math.abs


/**
 * The com.github.ajalt.mordant.rendering.markdown.main entry point into mordant. Used to generate ANSI codes.
 *
 * You typically want to use this in a `with` block. Colors and types can be nested, and will automatically be
 * reset.
 *
 * This will print the text "red white and blue", with foreground colors matching the text:
 *
 * ```kotlin
 * with(TerminalColors()) {
 *     println("${red("red")} ${white("white")} and ${blue("blue")}")
 * }
 * ```
 *
 *
 * This will set the background color while leaving the foreground unchanged:
 *
 * ```kotlin
 * with(TerminalColors()) {
 *     println("The foreground ${brightBlue.bg("color will stay the")} same")
 * }
 * ```
 *
 * This will set foreground and background:
 *
 * ```kotlin
 * with(TerminalColors()) {
 *     println((yellow on brightGreen)("this is easy to read, right?"))
 * }
 * ```
 *
 * You can also mix text styles with colors:
 *
 * ```kotlin
 * with(TerminalColors()) {
 *     println((bold + white + underline)("Listen!"))
 * }
 * ```
 */
class TerminalColors internal constructor(
        private val level: AnsiLevel = TerminalCapabilities.detectANSISupport()
) {
    val black: AnsiColorCode get() = ansi16(AnsiColor.black)
    val red: AnsiColorCode get() = ansi16(AnsiColor.red)
    val green: AnsiColorCode get() = ansi16(AnsiColor.green)
    val yellow: AnsiColorCode get() = ansi16(AnsiColor.yellow)
    val blue: AnsiColorCode get() = ansi16(AnsiColor.blue)
    val magenta: AnsiColorCode get() = ansi16(AnsiColor.magenta)
    val cyan: AnsiColorCode get() = ansi16(AnsiColor.cyan)
    val white: AnsiColorCode get() = ansi16(AnsiColor.white)
    val gray: AnsiColorCode get() = ansi16(AnsiColor.gray)

    val brightRed: AnsiColorCode get() = ansi16(AnsiColor.brightRed)
    val brightGreen: AnsiColorCode get() = ansi16(AnsiColor.brightGreen)
    val brightYellow: AnsiColorCode get() = ansi16(AnsiColor.brightYellow)
    val brightBlue: AnsiColorCode get() = ansi16(AnsiColor.brightBlue)
    val brightMagenta: AnsiColorCode get() = ansi16(AnsiColor.brightMagenta)
    val brightCyan: AnsiColorCode get() = ansi16(AnsiColor.brightCyan)
    val brightWhite: AnsiColorCode get() = ansi16(AnsiColor.brightWhite)

    // TODO: test parsing of single codes
    /** Clear all active styles */
    val reset: AnsiCode get() = style(AnsiStyle.reset)

    /**
     * Render text as bold or increased intensity.
     *
     * Might be rendered as a different color instead of a different font weight.
     */
    val bold: AnsiCode get() = style(AnsiStyle.bold)

    /**
     * Render text as faint or decreased intensity.
     *
     * Not widely supported.
     */
    val dim: AnsiCode get() = style(AnsiStyle.dim)

    /**
     * Render text as italic.
     *
     * Not widely supported, might be rendered as inverse instead of italic.
     */
    val italic: AnsiCode get() = style(AnsiStyle.italic)

    /**
     * Underline text.
     *
     * Might be rendered with different colors instead of underline.
     */
    val underline: AnsiCode get() = style(AnsiStyle.underline)

    /** Render text with background and foreground colors switched. */
    val inverse: AnsiCode get() = style(AnsiStyle.inverse)

    /**
     * Conceal text.
     *
     * Not widely supported.
     */
    val hidden: AnsiCode get() = style(AnsiStyle.hidden)

    /**
     * Render text with a strikethrough.
     *
     * Not widely supported.
     */
    val strikethrough: AnsiCode get() = style(AnsiStyle.strikethrough)

    /**
     * No style.
     */
    val plain: AnsiCode get() = AnsiCode(emptyList())

    /** @param hex An rgb hex string in the form "#ffffff" or "ffffff" */
    fun rgb(hex: String): AnsiColorCode = color(RGB(hex))

    /**
     * Create a color code from an RGB color.
     *
     * @param r The red amount, in the range \[0, 255]
     * @param g The green amount, in the range \[0, 255]
     * @param b The blue amount, in the range \[0, 255]
     */
    fun rgb(r: Int, g: Int, b: Int): AnsiColorCode = AnsiColor.rgb(r, g, b, level)

    /**
     * Create a color code from an HSL color.
     *
     * @param h The hue, in the range \[0, 360]
     * @param s The saturation, in the range \[0, 100]
     * @param l The lightness, in the range \[0, 100]
     */
    fun hsl(h: Int, s: Int, l: Int): AnsiColorCode = AnsiColor.hsl(h, s, l, level)

    /**
     * Create a color code from an HSV color.
     *
     * @param h The hue, in the range \[0, 360]
     * @param s The saturation, in the range \[0,100]
     * @param v The value, in the range \[0,100]
     */
    fun hsv(h: Int, s: Int, v: Int): AnsiColorCode = AnsiColor.hsv(h, s, v, level)

    /**
     * Create a color code from a CMYK color.
     *
     * @param c The cyan amount, in the range \[0, 100]
     * @param m The magenta amount, in the range \[0,100]
     * @param y The yellow amount, in the range \[0,100]
     * @param k The black amount, in the range \[0,100]
     */
    fun cmyk(c: Int, m: Int, y: Int, k: Int): AnsiColorCode = AnsiColor.cmyk(c, m, y, k, level)

    /**
     * Create a grayscale color code from a fraction in the range \[0, 1].
     *
     * @param fraction The fraction of white in the color. 0 is pure black, 1 is pure white.
     */
    fun gray(fraction: Double): AnsiColorCode = AnsiColor.gray(fraction, level)

    /**
     * Create a color code from a CIE XYZ color.
     *
     * Conversions use D65 reference white, and sRGB profile.
     *
     * [x], [y], and [z] are generally in the interval [0, 100], but may be larger
     */
    fun xyz(x: Double, y: Double, z: Double): AnsiColorCode = AnsiColor.xyz(x, y, z, level)


    /**
     * Create a color code from a CIE LAB color.
     *
     * Conversions use D65 reference white, and sRGB profile.
     *
     * [l] is in the interval [0, 100]. [a] and [b] have unlimited range,
     * but are generally in [-100, 100]
     */
    fun lab(l: Double, a: Double, b: Double): AnsiColorCode = AnsiColor.lab(l, a, b, level)

    /**
     * Create an ANSI code to move the cursor up [count] cells.
     *
     * If ANSI codes are not supported, or [count] is 0, an empty string is returned.
     * If [count] is negative, the cursor will be moved down instead.
     */
    fun cursorUp(count: Int): String = moveCursor(if (count < 0) "B" else "A", abs(count))

    /**
     * Create an ANSI code to move the cursor down [count] cells.
     *
     * If ANSI codes are not supported, or [count] is 0, an empty string is returned.
     * If [count] is negative, the cursor will be moved up instead.
     */
    fun cursorDown(count: Int): String = moveCursor(if (count < 0) "A" else "B", abs(count))

    /**
     * Create an ANSI code to move the cursor left [count] cells.
     *
     * If ANSI codes are not supported, or [count] is 0, an empty string is returned.
     * If [count] is negative, the cursor will be moved right instead.
     */
    fun cursorLeft(count: Int): String = moveCursor(if (count < 0) "C" else "D", abs(count))

    /**
     * Create an ANSI code to move the cursor right [count] cells.
     *
     * If ANSI codes are not supported, or [count] is 0, an empty string is returned.
     * If [count] is negative, the cursor will be moved left instead.
     */
    fun cursorRight(count: Int): String = moveCursor(if (count < 0) "D" else "C", abs(count))

    /**
     * Create an ANSI code to hide the cursor.
     *
     * If ANSI codes are not supported, an empty string is returned.
     */
    val hideCursor: String get() = if (level == AnsiLevel.NONE) "" else "$CSI?25l"

    /**
     * Create an ANSI code to show the cursor.
     *
     * If ANSI codes are not supported, an empty string is returned.
     */
    val showCursor: String get() = if (level == AnsiLevel.NONE) "" else "$CSI?25h"

    private fun moveCursor(dir: String, count: Int): String {
        return if (count == 0 || level == AnsiLevel.NONE) ""
        else "$CSI$count$dir"
    }

    private fun ansi16(color: AnsiColor) =
            if (level == AnsiLevel.NONE) DisabledAnsiColorCode else color.code

    private fun style(style: AnsiStyle) =
            if (level == AnsiLevel.NONE) DisabledAnsiCode else style.code

    /**
     * Create a color from an existing [ConvertibleColor].
     *
     * It's usually easier to use a function like [rgb] or [hsl] instead.
     */
    fun color(color: ConvertibleColor): AnsiColorCode = AnsiColor.color(color, level)
}
