package com.github.ajalt.mordant.rendering.table

data class Borders(val left: Boolean = false, val top: Boolean = false, val right: Boolean = false, val bottom: Boolean = false) {
    companion object {
        fun all(): Borders = Borders(left = true, top = true, right = true, bottom = true)
        fun none(): Borders = Borders(left = false, top = false, right = false, bottom = false)
        fun leftAndRight() = Borders(left = true, top = false, right = true, bottom = false)
        fun topAndBottom() = Borders(left = false, top = true, right = false, bottom = true)
    }
}
