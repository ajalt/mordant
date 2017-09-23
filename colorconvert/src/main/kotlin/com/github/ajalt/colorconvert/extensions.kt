package com.github.ajalt.colorconvert

internal fun Double.roundToInt() = Math.round(this).toInt()
internal fun Double.percentToInt() = (this * 100).roundToInt()
