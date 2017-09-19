package com.github.ajalt.termcolors

import com.github.ajalt.colorconvert.*


class TermColors(val level: Level = Level.TRUECOLOR) {
    enum class Level {NONE, ANSI16, ANSI256, TRUECOLOR }

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
    val reset
        get() = if (level == Level.NONE) DisabledAnsiCode else AnsiCode(intArrayOf(0), intArrayOf())

    /**
     * Render text as bold or increased intensity.
     *
     * Might be rendered as a different color instead of a different font weight.
     */
    val bold get() = ansi(1, 22)

    /**
     * Render text as faint or decreased intensity.
     *
     * Not widely supported.
     */
    val dim get() = ansi(2, 22)

    /**
     * Render text as italic.
     *
     * Not widely supported, might be rendered as inverse instead of italic.
     */
    val italic get() = ansi(3, 23)

    /**
     * Underline text.
     *
     * Might be rendered with different colors instead of underline.
     */
    val underline get() = ansi(4, 24)

    /** Render text with background and foreground colors switched. */
    val inverse get() = ansi(7, 27)

    /**
     * Conceal text.
     *
     * Not widely supported.
     */
    val hidden get() = ansi(8, 28)

    /**
     * Render text with a strikethrough.
     *
     * NOt widely supported.
     */
    val strikethrough get() = ansi(9, 29)

    /** @param hex An rgb hex string in the form "#ffffff" or "ffffff" */
    fun rgb(hex: String): AnsiColorCode = downsample(RGB(hex))

    fun rgb(r: Int, g: Int, b: Int): AnsiColorCode = downsample(RGB(r, g, b))
    fun hsl(h: Int, s: Int, l: Int): AnsiColorCode = downsample(HSL(h, s, l))
    fun hsv(h: Int, s: Int, v: Int): AnsiColorCode = downsample(HSV(h, s, v))

    /**
     * Return a grayscale color.
     *
     * @param fraction The fraction of white in the color. 0 is pure black, 1 is pure white.
     */
    fun gray(fraction: Double): AnsiColorCode {
        require(fraction in 0.0..1.0) { "fraction must be in the range [0, 1]" }
        return Math.round(255 * fraction).toInt().let { rgb(it, it, it) }
    }

    private fun ansi16(code: Int) =
            if (level == Level.NONE) DisabledAnsiColorCode else Ansi16ColorCode(code)

    private fun ansi(open: Int, close: Int) =
            if (level == Level.NONE) DisabledAnsiCode else AnsiCode(open, close)

    private fun downsample(color: ConvertibleColor): AnsiColorCode = when (level) {
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
