package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.CSI
import com.github.ajalt.mordant.rendering.internal.parseText

data class Span private constructor(val text: String, val style: TextStyle = TextStyle()) {
    init {
        require(text.isNotEmpty()) { "Span text cannot be empty" }
        require(text.count { it.isWhitespace() }.let { it == 0 || it == text.length }) {
            "Spans must contain ether all whitespace, or no whitespace: \"$text\""
        }
        require(CSI !in text) { "Spans cannot contain ANSI codes" }
    }

    internal companion object {
        fun parse(text: String, collapseNewlines: Boolean): List<Span> = parseText(text, collapseNewlines)
        fun word(text: String, style: TextStyle = TextStyle()) = Span(text, style)

        fun line() = Span("\n")
    }

    internal val cellWidth: Int = text.length // TODO: calculate cell width
    internal fun withStyle(style: TextStyle) = copy(style = style + this.style)
}
