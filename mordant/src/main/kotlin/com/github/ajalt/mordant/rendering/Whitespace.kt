package com.github.ajalt.mordant.rendering

enum class Whitespace(
        val collapseNewlines: Boolean,
        val collapseSpaces: Boolean,
        val wrap: Boolean,
        val trimEol: Boolean
) {
    NORMAL(collapseNewlines = true, collapseSpaces = true, wrap = true, trimEol = true),
    NOWRAP(collapseNewlines = true, collapseSpaces = true, wrap = false, trimEol = true),
    PRE(collapseNewlines = false, collapseSpaces = false, wrap = false, trimEol = false),
    PRE_WRAP(collapseNewlines = false, collapseSpaces = false, wrap = true, trimEol = true),
    PRE_LINE(collapseNewlines = false, collapseSpaces = true, wrap = true, trimEol = true)
}
