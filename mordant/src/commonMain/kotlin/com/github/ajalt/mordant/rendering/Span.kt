package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.internal.stringCellWidth
import kotlin.LazyThreadSafetyMode.NONE

internal val SINGLE_SPACE by lazy { Span.space(1) }

class Span private constructor(val text: String, val style: TextStyle = DEFAULT_STYLE) {
    init {
        require(text.isNotEmpty()) { "Span text cannot be empty" }
        require(text.count { it.isWhitespace() }.let { it == 0 || it == text.length }) {
            "Spans must contain ether all whitespace, or no whitespace: \"$text\""
        }
        require("\n" !in text) { "Spans cannot contain newlines" }
        require(CSI !in text) { "Spans cannot contain ANSI codes" }
    }

    internal companion object {
        fun word(text: String, style: TextStyle = DEFAULT_STYLE) = Span(text, style)
        fun space(width: Int = 1, style: TextStyle = DEFAULT_STYLE) = Span(" ".repeat(width), style)
    }

    internal val cellWidth: Int by lazy(NONE) { stringCellWidth(text) }
    internal fun take(n: Int): Span = Span(text.take(n), style)

    internal fun isWhitespace(): Boolean = text[0].isWhitespace()
    internal fun isTab(): Boolean = text[0] == '\t'

    internal fun withStyle(style: TextStyle) = Span(text, this.style + style)
    internal fun replaceStyle(style: TextStyle) = Span(text, style)
}
