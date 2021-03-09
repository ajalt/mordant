package com.github.ajalt.mordant.rendering

/**
 * Setting for handling of long words that exceed the line length by themselves.
 *
 * These values correspond to the values of the CSS
 * [overflow-wrap][https://developer.mozilla.org/en-US/docs/Web/CSS/overflow-wrap] property.
 */
enum class OverflowWrap {
    /** Don't break or alter long words */
    NORMAL,
    /** Break words that exceed the maximum line length */
    BREAK_WORD,
    /** Truncate words that exceed the maximum line length */
    TRUNCATE,
    /** Truncate words that exceed the maximum line length, and replace the last visible character with `…` */
    ELLIPSES
}
