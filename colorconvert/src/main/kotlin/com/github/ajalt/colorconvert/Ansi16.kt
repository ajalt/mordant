package com.github.ajalt.colorconvert

data class Ansi16(val code: Int) : ConvertibleColor {
    init {
        require(code in 30..37 || code in 40..47 ||
                code in 90..97 || code in 100..107) {
            "code not valid: $code"
        }
    }

    companion object {
        val black: Ansi16 get() = Ansi16(30)
        val red: Ansi16 get() = Ansi16(31)
        val green: Ansi16 get() = Ansi16(32)
        val yellow: Ansi16 get() = Ansi16(33)
        val blue: Ansi16 get() = Ansi16(34)
        val purple: Ansi16 get() = Ansi16(35)
        val cyan: Ansi16 get() = Ansi16(36)
        val white: Ansi16 get() = Ansi16(37)

        val brightBlack: Ansi16 get() = Ansi16(90)
        val brightRed: Ansi16 get() = Ansi16(91)
        val brightGreen: Ansi16 get() = Ansi16(92)
        val brightYellow: Ansi16 get() = Ansi16(93)
        val brightBlue: Ansi16 get() = Ansi16(94)
        val brightPurple: Ansi16 get() = Ansi16(95)
        val brightCyan: Ansi16 get() = Ansi16(96)
        val brightWhite: Ansi16 get() = Ansi16(97)
    }

    override fun toRGB(): RGB {
        val color = code % 10

        // grayscale
        if (color == 0 || color == 7) {
            val c: Double =
                    if (code > 50) color + 3.5
                    else color.toDouble()

            val v = (c / 10.5 * 255).roundToInt()

            return RGB(v, v, v)
        }

        // color
        val mul = if (code > 50) 1.0 else 0.5
        val r = ((color % 2) * mul) * 255
        val g = (((color / 2) % 2) * mul) * 255
        val b = (((color / 4) % 2) * mul) * 255

        return RGB(r.roundToInt(), g.roundToInt(), b.roundToInt())
    }

    override fun toAnsi16() = this
    override fun toAnsi256() = when {
        code >= 90 -> Ansi256(code - 90 + 8)
        else -> Ansi256(code - 30)
    }
}
