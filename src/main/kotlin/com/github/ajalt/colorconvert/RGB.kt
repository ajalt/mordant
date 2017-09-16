package com.github.ajalt.colorconvert

data class RGB(val r: Int, val g: Int, val b: Int) {
    fun toHSL(): HSL {
        val r = this.r / 255.0
        val g = this.g / 255.0
        val b = this.b / 255.0
        val min = minOf(r, g, b)
        val max = maxOf(r, g, b)
        val delta = max - min
        var h = when {
            max == min -> 0.0
            r == max -> (g - b) / delta
            g == max -> 2 + (b - r) / delta
            b == max -> 4 + (r - g) / delta
            else -> 0.0
        }

        h = minOf(h * 60, 360.0)
        if (h < 0) h += 360
        val l = (min + max) / 2
        val s = when {
            max == min -> 0.0
            l <= 0.5 -> delta / (max + min)
            else -> delta / (2 - max - min)
        }

        return HSL(h.toInt(), (s * 100).toInt(), (l * 100).toInt())
    }

    fun toHSV(): HSV {
        val r = this.r.toDouble()
        val g = this.g.toDouble()
        val b = this.b.toDouble()
        val min = minOf(r, g, b)
        val max = maxOf(r, g, b)
        val delta = max - min

        val s = when (max) {
            0.0 -> 0.0
            else -> (delta / max * 1000) / 10
        }

        var h = when {
            max == min -> 0.0
            r == max -> (g - b) / delta
            g == max -> 2 + (b - r) / delta
            b == max -> 4 + (r - g) / delta
            else -> 0.0
        }

        h = minOf(h * 60, 360.0)

        if (h < 0) {
            h += 360
        }

        val v = ((max / 255) * 1000) / 10

        return HSV(h.toInt(), s.toInt(), v.toInt())
    }
}

