package com.github.ajalt.mordant.rendering

data class Span(val text: String, val styles: Set<TextStyle> = emptySet()) {
    companion object {
        fun line() = Span("\n")
    }
    fun withStyle(vararg styles: TextStyle) = copy(styles = this.styles + styles)
    fun withStyle(styles: Iterable<TextStyle>) = copy(styles = this.styles + styles)
}
