package com.github.ajalt.mordant

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.markdown.MarkdownRenderer

class Terminal(
        ansiLevel: AnsiLevel = TerminalCapabilities.detectANSISupport(),
        val theme: Theme = DEFAULT_THEME,
        val width: Int = currentWidth(),
        val tabWidth: Int = 8
) {
    companion object {
        /**
         * Return the current width in cells of the interactive terminal, or [default] if the
         * terminal is not interactive or its width cannot be determined.
         */
        fun currentWidth(default: Int = 79): Int = System.getenv("COLUMNS")?.toInt() ?: default
    }

    init {
        require(tabWidth >= 0) { "tab width cannot be negative" }
    }

    val colors: TerminalColors = TerminalColors(ansiLevel)

    fun printMarkdown(markdown: String, showHtml: Boolean = false) {
        return kotlin.io.print(renderMarkdown(markdown, showHtml))
    }

    fun renderMarkdown(markdown: String, showHtml: Boolean = false): String {
        return render(MarkdownRenderer(markdown, theme, showHtml).render())
    }

    fun parseMarkdown(markdown: String, showHtml: Boolean = false): Renderable {
        return MarkdownRenderer(markdown, theme, showHtml).render()
    }

    fun success(
            message: Any?,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ) {
        println(message, theme.success, whitespace, align, overflowWrap, width)
    }

    fun renderSuccess(
            message: Any?,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ): String {
        return render(message, theme.success, whitespace, align, overflowWrap, width)
    }

    fun danger(
            message: Any?,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ) {
        println(message, theme.danger, whitespace, align, overflowWrap, width)
    }

    fun renderDanger(
            message: Any?,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ): String {
        return render(message, theme.danger, whitespace, align, overflowWrap, width)
    }

    fun warning(
            message: Any?,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ) {
        println(message, theme.warning, whitespace, align, overflowWrap, width)
    }

    fun renderWarning(
            message: Any?,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ): String {
        return render(message, theme.warning, whitespace, align, overflowWrap, width)
    }

    fun info(
            message: Any?,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ) {
        println(message, theme.info, whitespace, align, overflowWrap, width)
    }

    fun renderInfo(
            message: Any?,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ): String {
        return render(message, theme.info, whitespace, align, overflowWrap, width)
    }

    fun muted(
            message: Any?,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ) {
        println(message, theme.muted, whitespace, align, overflowWrap, width)
    }

    fun renderMuted(
            message: Any?,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ): String {
        return render(message, theme.muted, whitespace, align, overflowWrap, width)
    }

    fun print(
            message: Any?,
            style: TextStyle = DEFAULT_STYLE,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ) {
        kotlin.io.print(render(message, style, whitespace, align, overflowWrap, width))
    }

    fun println() = kotlin.io.println()

    fun println(
            message: Any?,
            style: TextStyle = DEFAULT_STYLE,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ) {
        kotlin.io.println(render(message, style, whitespace, align, overflowWrap, width))
    }

    fun print(renderable: Renderable) {
        kotlin.io.print(render(renderable))
    }

    fun println(renderable: Renderable) {
        kotlin.io.println(render(renderable))
    }

    fun render(
            message: Any?,
            style: TextStyle = DEFAULT_STYLE,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ): String {
        return render(when (message) {
            is Renderable -> message
            else -> Text(message.toString(), style, whitespace, align, overflowWrap, width)
        })
    }

    fun render(renderable: Renderable): String {
        return renderLines(renderable.render(this))
    }

    private fun renderLines(lines: Lines): String = buildString {
        for ((i, line) in lines.lines.withIndex()) {
            if (i > 0) append("\n")

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
