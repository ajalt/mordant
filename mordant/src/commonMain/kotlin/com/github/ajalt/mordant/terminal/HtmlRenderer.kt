package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.parseText
import com.github.ajalt.mordant.rendering.TextStyle

fun TerminalRecorder.outputAsHtml(): String = buildString {
    val lines = parseText(output(), DEFAULT_STYLE)

    appendLine("<html><body>")
    // font-family from https://systemfontstack.com/
    append("""<pre style="font-family: Menlo, Consolas, Monaco, Liberation Mono, Lucida Console, monospace">""")
    appendLine("<code>")

    for (line in lines.lines) {
        for (span in line) {
            val href = span.style.hyperlink
            if (href != null) {
                append("""<a href="""").append(href).append('"')
            } else {
                append("<span")
            }
            val rules = span.style.asCssRules()
            if (rules.isNotEmpty()) {
                rules.joinTo(this, "; ", prefix = " style=\"", postfix = "\"")
            }
            append(">")
            append(span.text.escapeHtml()).append("</span>")
        }
        appendLine()
    }

    appendLine("</code></pre>")
    append("</body></html>")
}

private fun TextStyle.asCssRules(): List<String> {
    val rules = mutableListOf<String>()
    val (fg, bg) = if (inverse == true) bgColor to color else color to bgColor
    fg?.let {
        rules.add("color: ${it.toSRGB().toHex()}")
    }
    bg?.let {
        rules.add("background-color: ${it.toSRGB().toHex()}")
    }
    if (bold == true) {
        rules.add("font-weight: bold")
    }
    if (italic == true) {
        rules.add("font-style: italic")
    }
    if (underline == true) {
        rules.add("text-decoration: underline")
    }
    if (dim == true) {
        rules.add("opacity: 0.5")
    }
    if (strikethrough == true) {
        rules.add("text-decoration: line-through")
    }
    return rules
}

private fun String.escapeHtml(): String {
    return replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("\\", "&#x27;")
}
