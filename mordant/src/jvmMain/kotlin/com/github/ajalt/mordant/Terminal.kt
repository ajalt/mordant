package com.github.ajalt.mordant

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.markdown.MarkdownRenderer

class Terminal(
        level: AnsiLevel = TerminalCapabilities.detectANSISupport(),
        val theme: Theme = DEFAULT_THEME,
        val width: Int = System.getenv("COLUMNS")?.toInt() ?: 79
) {
    val colors: TerminalColors = TerminalColors(level)

    fun printMarkdown(markdown: String, showHtml: Boolean = false) {
        return kotlin.io.print(renderMarkdown(markdown, showHtml))
    }

    fun renderMarkdown(markdown: String, showHtml: Boolean = false): String {
        return render(MarkdownRenderer(markdown, theme, showHtml).render())
    }

    fun parseMarkdown(markdown: String, showHtml: Boolean = false): Renderable {
        return MarkdownRenderer(markdown, theme, showHtml).render()
    }

    // TODO: add options like width, padding, whitespace
    fun print(text: String) {
        kotlin.io.print(render(text))
    }

    fun print(renderable: Renderable) {
        kotlin.io.print(render(renderable))
    }

    fun render(text: String): String {
        return render(Text(text, whitespace = Whitespace.PRE_WRAP))
    }

    fun render(renderable: Renderable): String {
        return renderLines(renderable.render(this))
    }

   private fun renderLines(lines: Lines): String = buildString {
        for ((i, line) in lines.lines.withIndex()) {
            if (i > 0) append("\n") // TODO: line separator

            // Concat equal ansi codes to avoid closing and reopening them on every span
            var activeCode: AnsiCode? = null
            for (span in line) {
                val code = span.style.toAnsi(this@Terminal)
                if (code != activeCode) {
                    if (activeCode != null) append(activeCode.close)
                    activeCode = code
                    append(code.open)
                }
                append(span.text)
            }
            activeCode?.let { append(it.close) }
        }
    }
}

private fun TextStyle.toAnsi(t: Terminal): AnsiCode {
    var code = color?.let { t.colors.color(it) } ?: t.colors.plain

    if (bgColor != null) code += t.colors.color(bgColor).bg
    if (bold) code += t.colors.bold
    if (italic) code += t.colors.italic
    if (underline) code += t.colors.underline
    if (dim) code += t.colors.dim
    if (inverse) code += t.colors.inverse
    if (strikethrough) code += t.colors.strikethrough

    return code
}
