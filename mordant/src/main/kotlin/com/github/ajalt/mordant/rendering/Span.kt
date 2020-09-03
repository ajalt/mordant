package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.CSI
import com.github.ajalt.mordant.rendering.internal.parseText

@Suppress("DataClassPrivateConstructor")
data class Span private constructor(val text: String, val style: TextStyle = TextStyle()) {
    init {
        require(text.isNotEmpty()) { "Span text cannot be empty" }
        require(text.count { it.isWhitespace() }.let { it == 0 || it == text.length }) {
            "Spans must contain ether all whitespace, or no whitespace: \"$text\""
        }
        require("\n" !in text) { "Spans cannot contain newlines" }
        require(CSI !in text) { "Spans cannot contain ANSI codes" }
    }

    internal companion object {
        fun parse(text: String): Lines = parseText(text)

        // TODO: maybe just inline this
        fun word(text: String, style: TextStyle = TextStyle()) = Span(text, style)

        @Deprecated("lines")// TODO
        fun line() = Span("\n")
    }


    // TODO: calculate cell width
    internal val cellWidth: Int = if (text.endsWith("\n")) text.length - 1 else text.length
    internal fun take(n: Int): Span = copy(text = text.take(n))

    internal fun isWhitespace() : Boolean = text.first().isWhitespace()

    internal fun withStyle(style: TextStyle) = copy(style = style + this.style)
}
