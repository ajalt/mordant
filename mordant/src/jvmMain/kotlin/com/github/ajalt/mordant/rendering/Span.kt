package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.CSI

internal val SINGLE_SPACE = Span.space(1)

@Suppress("DataClassPrivateConstructor")
data class Span private constructor(val text: String, val style: TextStyle = DEFAULT_STYLE) {
    init {
        require(text.isNotEmpty()) { "Span text cannot be empty" }
        require(text.count { it.isWhitespace() }.let { it == 0 || it == text.length }) {
            "Spans must contain ether all whitespace, or no whitespace: \"$text\""
        }
        require("\n" !in text) { "Spans cannot contain newlines" }
        require(CSI !in text) { "Spans cannot contain ANSI codes" }
    }

    internal companion object {
        // TODO: maybe just inline this
        fun word(text: String, style: TextStyle = DEFAULT_STYLE) = Span(text, style)
        fun space(width: Int = 1, style: TextStyle = DEFAULT_STYLE) = Span(" ".repeat(width), style)
    }


    // TODO: calculate cell width
    internal val cellWidth: Int = if (text.endsWith("\n")) text.length - 1 else text.length
    internal fun take(n: Int): Span = copy(text = text.take(n))

    internal fun isWhitespace() : Boolean = text.first().isWhitespace()

    internal fun withStyle(style: TextStyle) = copy(style = style + this.style)
}
