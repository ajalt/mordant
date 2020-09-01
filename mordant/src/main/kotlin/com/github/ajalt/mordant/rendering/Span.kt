package com.github.ajalt.mordant.rendering

data class Span(val text: String, val styles: Set<TextStyle> = emptySet()) {
    companion object {
        fun line() = Span("\n")
    }

    fun withStyle(vararg styles: TextStyle) = copy(styles = this.styles + styles)
    fun withStyle(styles: Iterable<TextStyle>) = copy(styles = this.styles + styles)

    /** Split span into words, spaces, and newlines*/
    internal fun split(collapseNewlines: Boolean): Sequence<Span> {
        val re = if (collapseNewlines) SPLIT_REGEX_COLLAPSE_NL else SPLIT_REGEX
        return re.findAll(text).map { copy(text = it.value) }
    }
}

private val SPLIT_REGEX = Regex("""\r?\n|\s+|\S+""")
private val SPLIT_REGEX_COLLAPSE_NL = Regex("""\s+|\S+""")
