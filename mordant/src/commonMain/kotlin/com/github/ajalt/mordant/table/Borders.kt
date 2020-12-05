package com.github.ajalt.mordant.table

enum class Borders(
        val left: Boolean,
        val top: Boolean,
        val right: Boolean,
        val bottom: Boolean
) {
    NONE(left = false, top = false, right = false, bottom = false),
    BOTTOM(left = false, top = false, right = false, bottom = true),
    RIGHT(left = false, top = false, right = true, bottom = false),
    BOTTOM_RIGHT(left = false, top = false, right = true, bottom = true),
    TOP(left = false, top = true, right = false, bottom = false),
    TOM_BOTTOM(left = false, top = true, right = false, bottom = true),
    TOP_RIGHT(left = false, top = true, right = true, bottom = false),
    TOP_RIGHT_BOTTOM(left = false, top = true, right = true, bottom = true),
    LEFT(left = true, top = false, right = false, bottom = false),
    LEFT_BOTTOM(left = true, top = false, right = false, bottom = true),
    LEFT_RIGHT(left = true, top = false, right = true, bottom = false),
    LEFT_RIGHT_BOTTOM(left = true, top = false, right = true, bottom = true),
    LEFT_TOP(left = true, top = true, right = false, bottom = false),
    LEFT_TOP_BOTTOM(left = true, top = true, right = false, bottom = true),
    LEFT_TOP_RIGHT(left = true, top = true, right = true, bottom = false),
    ALL(left = true, top = true, right = true, bottom = true),
}
