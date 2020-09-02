package com.github.ajalt.mordant

import com.github.ajalt.colormath.*
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * The com.github.ajalt.mordant.rendering.markdown.main entry point into mordant. Used to generate ANSI codes.
 *
 * You typically want to use this in a `with` block. Colors and types can be nested, and will automatically be
 * reset.
 *
 * This will print the text "red white and blue", with foreground colors matching the text:
 *
 * ```kotlin
 * with(TermColors()) {
 *     println("${red("red")} ${white("white")} and ${blue("blue")}")
 * }
 * ```
 *
 *
 * This will set the background color while leaving the foreground unchanged:
 *
 * ```kotlin
 * with(TermColors()) {
 *     println("The foreground ${brightBlue.bg("color will stay the")} same")
 * }
 * ```
 *
 * This will set foreground and background:
 *
 * ```kotlin
 * with(TermColors()) {
 *     println((yellow on brightGreen)("this is easy to read, right?"))
 * }
 * ```
 *
 * You can also mix text styles with colors:
 *
 * ```kotlin
 * with(TermColors()) {
 *     println((bold + white + underline)("Listen!"))
 * }
 * ```
 */
class TermColors(val level: Level = TerminalCapabilities.detectANSISupport()) {
    enum class Level { NONE, ANSI16, ANSI256, TRUECOLOR }

    val black: AnsiColorCode get() = ansi16(30)
    val red: AnsiColorCode get() = ansi16(31)
    val green: AnsiColorCode get() = ansi16(32)
    val yellow: AnsiColorCode get() = ansi16(33)
    val blue: AnsiColorCode get() = ansi16(34)
    val magenta: AnsiColorCode get() = ansi16(35)
    val cyan: AnsiColorCode get() = ansi16(36)
    val white: AnsiColorCode get() = ansi16(37)
    val gray: AnsiColorCode get() = ansi16(90)

    val brightRed: AnsiColorCode get() = ansi16(91)
    val brightGreen: AnsiColorCode get() = ansi16(92)
    val brightYellow: AnsiColorCode get() = ansi16(93)
    val brightBlue: AnsiColorCode get() = ansi16(94)
    val brightMagenta: AnsiColorCode get() = ansi16(95)
    val brightCyan: AnsiColorCode get() = ansi16(96)
    val brightWhite: AnsiColorCode get() = ansi16(97)

    /** Clear all active styles */
    val reset: AnsiCode get() = ansi(AnsiCodes.reset, AnsiCodes.reset)

    /**
     * Render text as bold or increased intensity.
     *
     * Might be rendered as a different color instead of a different font weight.
     */
    val bold: AnsiCode get() = ansi(AnsiCodes.boldOpen, AnsiCodes.boldClose)

    /**
     * Render text as faint or decreased intensity.
     *
     * Not widely supported.
     */
    val dim: AnsiCode get() = ansi(AnsiCodes.dimOpen, AnsiCodes.dimClose)

    /**
     * Render text as italic.
     *
     * Not widely supported, might be rendered as inverse instead of italic.
     */
    val italic: AnsiCode get() = ansi(AnsiCodes.italicOpen, AnsiCodes.italicClose)

    /**
     * Underline text.
     *
     * Might be rendered with different colors instead of underline.
     */
    val underline: AnsiCode get() = ansi(AnsiCodes.underlineOpen, AnsiCodes.underlineClose)

    /** Render text with background and foreground colors switched. */
    val inverse: AnsiCode get() = ansi(AnsiCodes.inverseOpen, AnsiCodes.inverseClose)

    /**
     * Conceal text.
     *
     * Not widely supported.
     */
    val hidden: AnsiCode get() = ansi(AnsiCodes.hiddenOpen, AnsiCodes.hiddenClose)

    /**
     * Render text with a strikethrough.
     *
     * Not widely supported.
     */
    val strikethrough: AnsiCode get() = ansi(AnsiCodes.strikethroughOpen, AnsiCodes.strikethroughClose)

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
    fun rgb(r: Int, g: Int, b: Int): AnsiColorCode = color(RGB(r, g, b))

    /**
     * Create a color code from an HSL color.
     *
     * @param h The hue, in the range \[0, 360]
     * @param s The saturation, in the range \[0, 100]
     * @param l The lightness, in the range \[0, 100]
     */
    fun hsl(h: Int, s: Int, l: Int): AnsiColorCode = color(HSL(h, s, l))

    /**
     * Create a color code from an HSV color.
     *
     * @param h The hue, in the range \[0, 360]
     * @param s The saturation, in the range \[0,100]
     * @param v The value, in the range \[0,100]
     */
    fun hsv(h: Int, s: Int, v: Int): AnsiColorCode = color(HSV(h, s, v))

    /**
     * Create a color code from a CMYK color.
     *
     * @param c The cyan amount, in the range \[0, 100]
     * @param m The magenta amount, in the range \[0,100]
     * @param y The yellow amount, in the range \[0,100]
     * @param k The black amount, in the range \[0,100]
     */
    fun cmyk(c: Int, m: Int, y: Int, k: Int): AnsiColorCode = color(CMYK(c, m, y, k))

    /**
     * Create a grayscale color code from a fraction in the range \[0, 1].
     *
     * @param fraction The fraction of white in the color. 0 is pure black, 1 is pure white.
     */
    fun gray(fraction: Double): AnsiColorCode {
        require(fraction in 0.0..1.0) { "fraction must be in the range [0, 1]" }
        return (255 * fraction).roundToInt().let { rgb(it, it, it) }
    }

    /**
     * Create a color code from a CIE XYZ color.
     *
     * Conversions use D65 reference white, and sRGB profile.
     *
     * [x], [y], and [z] are generally in the interval [0, 100], but may be larger
     */
    fun xyz(x: Double, y: Double, z: Double): AnsiColorCode = color(XYZ(x, y, z))


    /**
     * Create a color code from a CIE LAB color.
     *
     * Conversions use D65 reference white, and sRGB profile.
     *
     * [l] is in the interval [0, 100]. [a] and [b] have unlimited range,
     * but are generally in [-100, 100]
     */
    fun lab(l: Double, a: Double, b: Double): AnsiColorCode = color(LAB(l, a, b))

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
    val hideCursor: String get() = if (level == Level.NONE) "" else "$CSI?25l"

    /**
     * Create an ANSI code to show the cursor.
     *
     * If ANSI codes are not supported, an empty string is returned.
     */
    val showCursor: String get() = if (level == Level.NONE) "" else "$CSI?25h"

    private fun moveCursor(dir: String, count: Int): String {
        return if (count == 0 || level == Level.NONE) ""
        else "$CSI$count$dir"
    }

    private fun ansi16(code: Int) =
            if (level == Level.NONE) DisabledAnsiColorCode else Ansi16ColorCode(code)

    private fun ansi(open: Int, close: Int) =
            if (level == Level.NONE) DisabledAnsiCode else AnsiCode(open, close)

    /**
     * Create a color from an existing [ConvertibleColor].
     *
     * It's usually easier to use a function like [rgb] or [hsl] instead.
     */
    fun color(color: ConvertibleColor): AnsiColorCode = when (level) {
        Level.NONE -> DisabledAnsiColorCode
        Level.ANSI16 -> Ansi16ColorCode(color.toAnsi16().code)
        Level.ANSI256 ->
            if (color is Ansi16) Ansi16ColorCode(color.code)
            else Ansi256ColorCode(color.toAnsi256().code)
        Level.TRUECOLOR -> when (color) {
            is Ansi16 -> Ansi16ColorCode(color.code)
            is Ansi256 -> Ansi256ColorCode(color.code)
            else -> color.toRGB().run { AnsiRGBColorCode(r, g, b) }
        }
    }
}
