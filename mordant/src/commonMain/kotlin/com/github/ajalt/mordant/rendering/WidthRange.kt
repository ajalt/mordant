package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.terminal.Terminal

data class WidthRange(val min: Int, val max: Int) {
    init {
        require(min <= max) { "Range min cannot be larger than max" }
    }

    operator fun plus(extra: Int) = WidthRange(min + extra, max + extra)
    operator fun plus(other: WidthRange) = WidthRange(min + other.min, max + other.max)
    operator fun div(divisor: Int) = WidthRange(min / divisor, max / divisor)
}

internal fun Iterable<Renderable>.maxWidthRange(
        t: Terminal,
        width: Int,
        paddingWidth: Int = 0
): WidthRange {
    return maxWidthRange(paddingWidth) { it.measure(t, width - paddingWidth) }
}

internal inline fun <T> Iterable<T>.maxWidthRange(
        paddingWidth: Int = 0,
        mapping: (T) -> WidthRange?
): WidthRange {
    var max = 0
    var min = 0
    for (it in this) {
        val range = mapping(it) ?: continue
        max = maxOf(max, range.max)
        min = maxOf(min, range.min)
    }
    return WidthRange(min + paddingWidth, max + paddingWidth)
}
