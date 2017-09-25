package com.github.ajalt.colorconvert

data class Ansi256(val code: Int) : ConvertibleColor {
    init {
        check(code in 0..255) { "code must be in range [0,255]: $code" }
    }

    override fun toRGB(): RGB {
        // ansi16 colors
        if (code < 16) return toAnsi16().toRGB()

        // grayscale
        if (code >= 232) {
            val c = (code - 232) * 10 + 8
            return RGB(c, c, c)
        }

        // color
        val c = code - 16
        val rem = c % 36
        val r = Math.floor(c / 36.0) / 5.0 * 255
        val g = Math.floor(rem / 6.0) / 5.0 * 255
        val b = (rem % 6) / 5.0 * 255
        return RGB(r.roundToInt(), g.roundToInt(), b.roundToInt())
    }

    override fun toAnsi256() = this
    // 0-7 are standard ansi16 colors
    // 8-15 are bright ansi16 colors
    override fun toAnsi16() = when {
        code < 8 -> Ansi16(code + 30)
        code < 16 -> Ansi16(code - 8 + 90)
        else -> toRGB().toAnsi16()
    }
}
