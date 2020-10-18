package com.github.ajalt.mordant

import com.github.ajalt.colormath.*
import com.github.ajalt.mordant.AnsiCodes.bgColorReset
import com.github.ajalt.mordant.AnsiCodes.bgColorSelector
import com.github.ajalt.mordant.AnsiCodes.fgBgOffset
import com.github.ajalt.mordant.AnsiCodes.fgColorReset
import com.github.ajalt.mordant.AnsiCodes.fgColorSelector
import com.github.ajalt.mordant.AnsiCodes.selector256
import com.github.ajalt.mordant.AnsiCodes.selectorRgb
import com.github.ajalt.mordant.AnsiLevel.*
import kotlin.math.roundToInt

internal object AnsiCodes {
    val fg16Range = 30..37
    val fg16BrightRange = 90..97
    const val fgColorSelector = 38
    const val fgColorReset = 39

    const val fgBgOffset = 10

    val bg16Range = 40..47
    val bg16BrightRange = 100..107
    const val bgColorSelector = 48
    const val bgColorReset = 49

    const val selector256 = 5
    const val selectorRgb = 2

    const val underlineColorSelector = 58
}


internal const val ESC = "\u001B"

/** Control Sequence Introducer */
internal const val CSI = "$ESC["
/** Operating System Command */
internal const val OSC = "$ESC]"

private val ANSI_CSI_RE = Regex("""$ESC\[((?:\d{1,3};?)+)m""")

enum class AnsiLevel { NONE, ANSI16, ANSI256, TRUECOLOR }

interface AnsiCodeContainer {
    val code: AnsiCode

    operator fun invoke(text: String) = code.invoke(text)
    operator fun plus(other: AnsiCode) = code + other
    operator fun plus(other: AnsiCodeContainer) = code + other.code
}

interface AnsiColorCodeContainer : AnsiCodeContainer {
    override val code: Ansi16ColorCode

    /**
     * Get a color for background only.
     *
     * Note that if you want to specify both a background and foreground color, use [on] instead of
     * this property.
     */
    val bg: AnsiCode get() = code.bg

    infix fun on(bg: AnsiColorCode): AnsiCode = code on bg
    infix fun on(bg: AnsiColorCodeContainer): AnsiCode = code on bg.code
}

@Suppress("EnumEntryName")
enum class AnsiStyle(override val code: SingleAnsiCode) : AnsiCodeContainer {
    reset(SingleAnsiCode(0, 0)),
    bold(SingleAnsiCode(1, 22)),
    dim(SingleAnsiCode(2, 22)),
    italic(SingleAnsiCode(3, 23)),
    underline(SingleAnsiCode(4, 24)),
    inverse(SingleAnsiCode(7, 27)),
    hidden(SingleAnsiCode(8, 28)),
    strikethrough(SingleAnsiCode(9, 29));

    val openCode: Int = code.openCode
    val closeCode: Int = code.closeCode

    override fun toString() = code.toString()
}

@Suppress("EnumEntryName")
enum class AnsiColor(override val code: Ansi16ColorCode) : AnsiColorCodeContainer, ConvertibleColor by code.color {
    black(Ansi16ColorCode(30)),
    red(Ansi16ColorCode(31)),
    green(Ansi16ColorCode(32)),
    yellow(Ansi16ColorCode(33)),
    blue(Ansi16ColorCode(34)),
    magenta(Ansi16ColorCode(35)),
    cyan(Ansi16ColorCode(36)),
    white(Ansi16ColorCode(37)),
    gray(Ansi16ColorCode(90)),

    brightRed(Ansi16ColorCode(91)),
    brightGreen(Ansi16ColorCode(92)),
    brightYellow(Ansi16ColorCode(93)),
    brightBlue(Ansi16ColorCode(94)),
    brightMagenta(Ansi16ColorCode(95)),
    brightCyan(Ansi16ColorCode(96)),
    brightWhite(Ansi16ColorCode(97));

    val color: Ansi16 get() = code.color

    override fun toString() = code.toString()

    companion object {
        /** @param hex An rgb hex string in the form "#ffffff" or "ffffff" */
        fun rgb(hex: String, level: AnsiLevel = TRUECOLOR): AnsiColorCode = color(RGB(hex), level)

        /**
         * Create a color code from an RGB color.
         *
         * @param r The red amount, in the range \[0, 255]
         * @param g The green amount, in the range \[0, 255]
         * @param b The blue amount, in the range \[0, 255]
         */
        fun rgb(r: Int, g: Int, b: Int, level: AnsiLevel = TRUECOLOR): AnsiColorCode = color(RGB(r, g, b), level)

        /**
         * Create a color code from an HSL color.
         *
         * @param h The hue, in the range \[0, 360]
         * @param s The saturation, in the range \[0, 100]
         * @param l The lightness, in the range \[0, 100]
         */
        fun hsl(h: Int, s: Int, l: Int, level: AnsiLevel = TRUECOLOR): AnsiColorCode = color(HSL(h, s, l), level)

        /**
         * Create a color code from an HSV color.
         *
         * @param h The hue, in the range \[0, 360]
         * @param s The saturation, in the range \[0,100]
         * @param v The value, in the range \[0,100]
         */
        fun hsv(h: Int, s: Int, v: Int, level: AnsiLevel = TRUECOLOR): AnsiColorCode = color(HSV(h, s, v), level)

        /**
         * Create a color code from a CMYK color.
         *
         * @param c The cyan amount, in the range \[0, 100]
         * @param m The magenta amount, in the range \[0,100]
         * @param y The yellow amount, in the range \[0,100]
         * @param k The black amount, in the range \[0,100]
         */
        fun cmyk(c: Int, m: Int, y: Int, k: Int, level: AnsiLevel = TRUECOLOR): AnsiColorCode = color(CMYK(c, m, y, k), level)

        /**
         * Create a grayscale color code from a fraction in the range \[0, 1].
         *
         * @param fraction The fraction of white in the color. 0 is pure black, 1 is pure white.
         */
        fun gray(fraction: Double, level: AnsiLevel = TRUECOLOR): AnsiColorCode {
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
        fun xyz(x: Double, y: Double, z: Double, level: AnsiLevel = TRUECOLOR): AnsiColorCode = color(XYZ(x, y, z), level)


        /**
         * Create a color code from a CIE LAB color.
         *
         * Conversions use D65 reference white, and sRGB profile.
         *
         * [l] is in the interval [0, 100]. [a] and [b] have unlimited range,
         * but are generally in [-100, 100]
         */
        fun lab(l: Double, a: Double, b: Double, level: AnsiLevel = TRUECOLOR): AnsiColorCode = color(LAB(l, a, b), level)


        /**
         * Create a color from an existing [ConvertibleColor].
         *
         * It's usually easier to use a function like [rgb] or [hsl] instead.
         */
        fun color(color: ConvertibleColor, level: AnsiLevel = TRUECOLOR): AnsiColorCode {
            val c = if (color is AnsiColorCodeContainer) color.code.color else color
            return when (level) {
                NONE -> DisabledAnsiColorCode
                ANSI16 -> Ansi16ColorCode(color.toAnsi16().code)
                ANSI256 ->
                    if (c is Ansi16) Ansi16ColorCode(c.code)
                    else Ansi256ColorCode(c.toAnsi256().code)
                TRUECOLOR -> when (c) {
                    is Ansi16 -> Ansi16ColorCode(c.code)
                    is Ansi256 -> Ansi256ColorCode(c.code)
                    else -> c.toRGB().run { AnsiRGBColorCode(r, g, b) }
                }
            }
        }
    }
}

/**
 * A class representing one or more numeric ANSI codes.
 *
 * @property codes A list of pairs, with each pair being the list of opening codes and a closing code.
 */
open class AnsiCode(protected val codes: List<Pair<List<Int>, Int>>) : (String) -> String {
    constructor(openCodes: List<Int>, closeCode: Int) : this(listOf(openCodes to closeCode))
    constructor(openCode: Int, closeCode: Int) : this(listOf(openCode), closeCode)

    val open: String get() = tag(codes.flatMap { it.first })
    val close: String get() = tag(codes.map { it.second })

    override fun invoke(text: String) = if (text.isEmpty()) "" else open + nest(text) + close

    open operator fun plus(other: AnsiCode) = AnsiCode(codes + other.codes)
    open operator fun plus(other: AnsiCodeContainer) = AnsiCode(codes + other.code.codes)

    private fun nest(text: String) = ANSI_CSI_RE.replace(text) { match ->
        // Replace instances of our close codes with their corresponding opening codes. If the close
        // code is at the end of the text, omit it instead so that we don't open and immediately
        // close a command.
        val openCodesByCloseCode = HashMap<Int, List<Int>>()
        for ((o, c) in codes) openCodesByCloseCode[c] = o
        val atEnd = match.range.last == text.lastIndex
        val codes = match.groupValues[1].splitToSequence(';').flatMap { code ->
            code.toInt().let {
                if (atEnd && it in openCodesByCloseCode) emptySequence()
                else (openCodesByCloseCode[it]?.asSequence() ?: sequenceOf(it))
            }
        }

        tag(codes.toList())
    }

    private fun tag(c: List<Int>) = if (c.isEmpty()) "" else "$CSI${c.joinToString(";")}m"

    override fun toString() = open

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AnsiCode
        return codes == other.codes
    }

    override fun hashCode() = codes.hashCode()
}

class SingleAnsiCode(val openCode: Int, val closeCode: Int) : AnsiCode(listOf(openCode), closeCode)

internal object DisabledAnsiCode : AnsiCode(emptyList()) {
    override fun plus(other: AnsiCode): AnsiCode = this
}

/**
 * A class representing one or more ANSI color codes.
 */
abstract class AnsiColorCode internal constructor(
        codes: List<Pair<List<Int>, Int>>
) : AnsiCode(codes) {
    constructor(openCodes: List<Int>, closeCode: Int) : this(listOf(openCodes to closeCode))
    constructor(openCode: Int, closeCode: Int) : this(listOf(openCode), closeCode)

    /**
     * Get a color for background only.
     *
     * Note that if you want to specify both a background and foreground color, use [on] instead of
     * this property.
     */
    val bg: AnsiCode get() = AnsiCode(bgCodes)

    /**
     * Create a new Ansi code that sets this color as the foreground and [bg] as the background.
     */
    open infix fun on(bg: AnsiColorCode): AnsiCode {
        return AnsiCode(codes + bg.bgCodes)
    }

    /**
     * Create a new Ansi code that sets this color as the foreground and [bg] as the background.
     */
    infix fun on(bg: AnsiColorCodeContainer): AnsiCode {
        return AnsiCode(codes + (bg.code as AnsiColorCode).bgCodes)
    }

    protected abstract val bgCodes: List<Pair<List<Int>, Int>>
}

internal object DisabledAnsiColorCode : AnsiColorCode(emptyList()) {
    override val bgCodes: List<Pair<List<Int>, Int>> get() = emptyList()
    override fun plus(other: AnsiCode): AnsiCode = this
    override fun on(bg: AnsiColorCode): AnsiCode = DisabledAnsiCode
}

class Ansi16ColorCode(private val code: Int) : AnsiColorCode(code, fgColorReset) {
    val color get() = Ansi16(code)
    override val bgCodes get() = codes.map { listOf(it.first[0] + fgBgOffset) to bgColorReset }
}

class Ansi256ColorCode(private val code: Int) : AnsiColorCode(listOf(fgColorSelector, selector256, code), fgColorReset) {
    val color get() = Ansi256(code)
    override val bgCodes get() = codes.map { listOf(bgColorSelector, selector256, it.first[2]) to bgColorReset }
}

class AnsiRGBColorCode(private val r: Int, private val g: Int, private val b: Int) : AnsiColorCode(listOf(fgColorSelector, selectorRgb, r, g, b), fgColorReset) {
    val color get() = RGB(r, g, b)
    override val bgCodes get() = codes.map { (o, _) -> listOf(bgColorSelector, selectorRgb, o[2], o[3], o[4]) to bgColorReset }
}
