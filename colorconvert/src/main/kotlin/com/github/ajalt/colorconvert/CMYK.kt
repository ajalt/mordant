package com.github.ajalt.colorconvert

data class CMYK(val c: Int, val m: Int, val y: Int, val k: Int) : ConvertibleColor {
    override fun toRGB(): RGB {
        val c = this.c / 100.0
        val m = this.m / 100.0
        val y = this.y / 100.0
        val k = this.k / 100.0
        val r = 255 * (1 - c) * (1 - k)
        val g = 255 * (1 - m) * (1 - k)
        val b = 255 * (1 - y) * (1 - k)
        return RGB(r.roundToInt(), g.roundToInt(), b.roundToInt())
    }

    override fun toHex(withNumberSign: Boolean)= toRGB().toHex(withNumberSign)

    override fun toHSL() = toRGB().toHSL()

    override fun toHSV()= toRGB().toHSV()

    override fun toAnsi16()= toRGB().toAnsi16()

    override fun toAnsi256()= toRGB().toAnsi256()

    override fun toCMYK() = this
}
