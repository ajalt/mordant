package com.github.ajalt.mordant.terminal

import com.github.ajalt.colormath.Color
import com.github.ajalt.colormath.formatCssString
import com.github.ajalt.colormath.model.SRGB
import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.parseText
import com.github.ajalt.mordant.rendering.TextStyle

/**
 * Render the contents of this [TerminalRecorder] as an HTML document.
 *
 * @param includeFrame If true, the output will be wrapped in a terminal frame with a drop shadow.
 * @param includeBodyTag If true, the output will be wrapped in `<html>` and `<body>` tags.
 * @param backgroundColor The background color of the output. If `null`, the background color will be unset.
 */
fun TerminalRecorder.outputAsHtml(
    includeFrame: Boolean = true,
    includeBodyTag: Boolean = true,
    backgroundColor: Color? = SRGB("#0c0c0c"),
): String = buildString {
    val lines = parseText(output(), DEFAULT_STYLE)

    if(includeBodyTag) appendLine("<html><body>")
    append("""<pre style="""")
    // font-family from https://systemfontstack.com/
    append("font-family: Menlo, Consolas, Monaco, Liberation Mono, Lucida Console, monospace;")
    if (backgroundColor != null) {
        append("background-color: ${backgroundColor.formatCssString()};")
    }
    if (includeFrame) {
        append("border-radius: 8px;")
        append("width: fit-content;")
        append("padding: 0.5em 1em 0;")
        append("filter: drop-shadow(0.5em 0.5em 0.5em black);")
    }
    append(""""><code>""")

    if (includeFrame) {
        for (color in listOf(SRGB("#ff5f56"), SRGB("#ffbd2e"), SRGB("#27c93f"))) {
            append("""<span style="color: ${color.toHex()};">⏺ </span>""")
        }
    }
    appendLine()


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

    append("</code></pre>")
    if(includeBodyTag) append("\n</body></html>")
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
