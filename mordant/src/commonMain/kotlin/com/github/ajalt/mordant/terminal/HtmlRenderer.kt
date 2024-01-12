package com.github.ajalt.mordant.terminal

import com.github.ajalt.colormath.Color
import com.github.ajalt.colormath.formatCssString
import com.github.ajalt.colormath.model.SRGB
import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.parseText
import com.github.ajalt.mordant.rendering.Span
import com.github.ajalt.mordant.rendering.TextStyle

/**
 * Render the contents of this [TerminalRecorder] as an HTML document.
 *
 * @param includeBodyTag If true, the output will be wrapped in `<html><body>` tags.
 * @param includeCodeTag If true, the output will be wrapped in `<pre><code>` tags. If false, only `<pre>` will be used.
 * @param backgroundColor If given, the output will be wrapped in a terminal frame with this background color.
 */
fun TerminalRecorder.outputAsHtml(
    includeBodyTag: Boolean = true,
    includeCodeTag: Boolean = true,
    backgroundColor: Color? = SRGB("#0c0c0c"),
): String = buildString {
    val lines = parseText(output(), DEFAULT_STYLE)
    if (includeBodyTag) appendLine("<html><body>")
    if (backgroundColor != null) {
        append("""<div style="""")
        append("border-radius: 8px;")
        append("width: fit-content;")
        append("padding: 0.5em 1em;")
        append("filter: drop-shadow(0.5em 0.5em 0.5em black);")
        append("background-color: ${backgroundColor.formatCssString()};")
        append("""">\n<div style="margin: -0.75em 0px;font-size: 2em">""")
        for (color in listOf(SRGB("#ff5f56"), SRGB("#ffbd2e"), SRGB("#27c93f"))) {
            append("""<span style="color: ${color.toHex()};">⏺&nbsp;</span>""")
        }
        appendLine("</div>")
    }
    append("""<pre style="font-family: monospace">""")
    if (includeCodeTag) append("<code>")
    appendLine()

    for (line in lines.lines) {
        val collected = mutableListOf<Span>()
        for (span in line) {
            if (collected.lastOrNull()?.let { it.style != span.style } == true) {
                addSpansAsHtml(collected)
                collected.clear()
            }
            collected.add(span)
        }
        addSpansAsHtml(collected)
        appendLine()
    }

    if (includeCodeTag) append("</code>")
    append("</pre>")
    if (backgroundColor != null) append("\n</div>")
    if (includeBodyTag) append("\n</body></html>")
}

private fun StringBuilder.addSpansAsHtml(spans: List<Span>) {
    if (spans.isEmpty()) return
    val style = spans.last().style
    val href = style.hyperlink
    if (href != null) {
        append("""<a href="""").append(href).append('"')
    } else {
        append("<span")
    }
    val rules = style.asCssRules()
    if (rules.isNotEmpty()) {
        rules.joinTo(this, "; ", prefix = " style=\"", postfix = "\"")
    }
    append(">")
    spans.joinTo(this, "") { it.text.escapeHtml() }
    append("</span>")
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
