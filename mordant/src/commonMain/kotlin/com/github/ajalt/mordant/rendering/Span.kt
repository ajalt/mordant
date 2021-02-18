package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.stringCellWidth
import kotlin.LazyThreadSafetyMode.NONE

class Span private constructor(val text: String, val style: TextStyle = DEFAULT_STYLE) {
    internal companion object {
        fun word(text: String, style: TextStyle = DEFAULT_STYLE): Span {
            require(text.isNotEmpty()) { "Span text cannot be empty" }
            require(text.count { it.isWhitespace() }.let { it == 0 || it == text.length }) {
                "Spans must contain ether all whitespace, or no whitespace: \"$text\""
            }
            require("\n" !in text) { "Spans cannot contain newlines" }
            require(CSI !in text) { "Spans cannot contain ANSI codes" }
            return Span(text, style)
        }

        fun space(width: Int = 1, style: TextStyle = DEFAULT_STYLE) = Span(" ".repeat(width), style)
        fun raw(text: String): Span = Span(text, DEFAULT_STYLE)
    }

    internal val cellWidth: Int by lazy(NONE) { stringCellWidth(text) }
    internal fun take(n: Int): Span = Span(text.take(n), style)

    internal fun isWhitespace(): Boolean = text[0].isWhitespace()
    internal fun isTab(): Boolean = text[0] == '\t'

    internal fun withStyle(style: TextStyle) = Span(text, this.style + style)
    internal fun replaceStyle(style: TextStyle) = Span(text, style)
}
