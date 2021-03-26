package com.github.ajalt.mordant.rendering

/**
 * Settings for handling of whitespace and line wrapping.
 *
 * These values correspond to the values of the CSS
 * [white-space][https://developer.mozilla.org/en-US/docs/Web/CSS/white-space] property.
 *
 * @property collapseNewlines If true, line breaks in the text will be replaced with spaces.
 * @property collapseSpaces If true, consecutive spaces will be replaced with a single space.
 * @property wrap If true, lines will be broken by replacing spaces with line breaks where necessary
 *   to keep lines under the configured width.
 * @property trimEol If true, whitespace at the end of lines will be removed.
 * @see OverflowWrap
 */
enum class Whitespace(
    val collapseNewlines: Boolean,
    val collapseSpaces: Boolean,
    val wrap: Boolean,
    val trimEol: Boolean,
) {
    /** Wrap text and collapse all whitespaces and line breaks */
    NORMAL(collapseNewlines = true, collapseSpaces = true, wrap = true, trimEol = true),

    /** Collapse spaces and line breaks, but don't wrap lines. This will effectively put all text on a single line. */
    NOWRAP(collapseNewlines = true, collapseSpaces = true, wrap = false, trimEol = true),

    /** Make no changes to the input text */
    PRE(collapseNewlines = false, collapseSpaces = false, wrap = false, trimEol = false),

    /** Like [PRE], but will break long lines */
    PRE_WRAP(collapseNewlines = false, collapseSpaces = false, wrap = true, trimEol = true),

    /** Like [NORMAL], but preserves any line breaks from the input */
    PRE_LINE(collapseNewlines = false, collapseSpaces = true, wrap = true, trimEol = true)
}
