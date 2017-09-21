package com.github.ajalt.colorconvert

data class Ansi256(val code: Int) : ConvertibleColor {
    init {
        check(code in 16..255) { "code must be in range 0..255: $code" }
    }

    override fun toRGB(): RGB {
        // 232 - 255 is grayscale
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

    override fun toAnsi16() = toRGB().toAnsi16()
    override fun toAnsi256() = this
    override fun toHSL() = toRGB().toHSL()
    override fun toHSV() = toRGB().toHSV()
    override fun toHex(withNumberSign: Boolean) = toRGB().toHex(withNumberSign)
}
