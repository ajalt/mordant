package com.github.ajalt.mordant.rendering

data class Span(val text: String, val style: TextStyle = TextStyle()) {
    companion object {
        fun line() = Span("\n")
    }

    val cellWidth: Int = text.length // TODO: calculate cell width


    fun withStyle(style: TextStyle) = copy(style = style + this.style)


    /** Split span into words, spaces, and newlines*/
    internal fun split(collapseNewlines: Boolean): Sequence<Span> {
        val re = if (collapseNewlines) SPLIT_REGEX_COLLAPSE_NL else SPLIT_REGEX
        return re.findAll(text).map { copy(text = it.value) }
    }
}

private val SPLIT_REGEX = Regex("""\r?\n|\s+|\S+""")
private val SPLIT_REGEX_COLLAPSE_NL = Regex("""\s+|\S+""")
