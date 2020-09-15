package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal

data class WidthRange(val min: Int, val max: Int) {
    operator fun plus(extra: Int) = WidthRange(min + extra, max + extra)
}

internal fun Iterable<Renderable>.maxWidthRange(
        t: Terminal,
        width: Int,
        paddingWidth: Int = 0
): WidthRange {
    var max = 0
    var min = 0
    forEach {
        val range = it.measure(t, width - paddingWidth)
        max = maxOf(max, range.max)
        min = maxOf(min, range.min)
    }
    return WidthRange(min + paddingWidth, max + paddingWidth)
}
