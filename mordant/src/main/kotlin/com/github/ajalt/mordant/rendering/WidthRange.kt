package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal

data class WidthRange(val min: Int, val max: Int) {
    operator fun plus(extra: Int) = WidthRange(min + extra, max + extra)
}

internal fun List<Renderable>.maxWidthRange(
        t: Terminal,
        width: Int,
        paddingWidth: Int = 0
): WidthRange {
    var max = Int.MIN_VALUE
    var min = Int.MIN_VALUE
    forEach {
        val range = it.measure(t, width - paddingWidth)
        max = maxOf(max, range.max)
        min = maxOf(min, range.min)
    }
    return WidthRange(min + paddingWidth, max + paddingWidth)
}
