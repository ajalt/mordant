package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.stringCellWidth
import kotlin.LazyThreadSafetyMode.PUBLICATION

/**
 * A word or string of whitespace and an associated [style].
 *
 * A Span cannot be empty, cannot contain newlines or ANSI codes, and must must either be entirely
 * whitespace or contain no whitespace at all.
 */
class Span private constructor(val text: String, val style: TextStyle = DEFAULT_STYLE) {
    companion object {
        /**
         * Create a Span from a string and optional style.
         *
         * The [text] cannot contain whitespace.
         */
        fun word(text: String, style: TextStyle = DEFAULT_STYLE): Span {
            require(text.isNotEmpty()) { "Span text cannot be empty" }
            require(text.count { it.isWhitespace() }.let { it == 0 || it == text.length }) {
                "Spans must contain either all whitespace, or no whitespace: \"$text\""
            }
            require("\n" !in text) { "Spans cannot contain newlines" }
            require(CSI !in text) { "Spans cannot contain ANSI codes" }
            return Span(text, style)
        }

        /**
         * Create a Span containing [width] number of space characters
         */
        fun space(width: Int = 1, style: TextStyle = DEFAULT_STYLE): Span {
            return Span(" ".repeat(width), style)
        }
    }

    internal val cellWidth: Int by lazy(PUBLICATION) { stringCellWidth(text) }
    internal fun take(n: Int): Span = Span(text.take(n), style)
    internal fun drop(n: Int): Span = Span(text.drop(n), style)

    internal fun isWhitespace(): Boolean = text[0].isWhitespace()
    internal fun isTab(): Boolean = text[0] == '\t'

    internal fun withStyle(style: TextStyle) = Span(text, this.style + style)
    internal fun replaceStyle(style: TextStyle) = Span(text, style)
    override fun toString(): String {
        return "<Span '${text}', ${style}>"
    }
}
