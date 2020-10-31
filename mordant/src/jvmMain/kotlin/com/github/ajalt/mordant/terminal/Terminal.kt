package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.components.Text
import com.github.ajalt.mordant.markdown.MarkdownRenderer

interface Terminal {
    val theme: Theme
    val tabWidth: Int
    val info: TerminalInfo
    val colors: TerminalColors
    val cursor: TerminalCursor

    fun printMarkdown(markdown: String, showHtml: Boolean = false) {
        return rawPrint(renderMarkdown(markdown, showHtml))
    }

    fun renderMarkdown(markdown: String, showHtml: Boolean = false): String {
        return render(parseMarkdown(markdown, showHtml))
    }

    fun parseMarkdown(markdown: String, showHtml: Boolean = false): Renderable {
        return MarkdownRenderer(markdown, theme, showHtml, info.ansiHyperLinks).render()
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

    fun danger(
            message: Any?,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ) {
        println(message, theme.danger, whitespace, align, overflowWrap, width)
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

    fun muted(
            message: Any?,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ) {
        println(message, theme.muted, whitespace, align, overflowWrap, width)
    }

    fun print(
            message: Any?,
            style: TextStyle = DEFAULT_STYLE,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ) {
        rawPrint(render(message, style, whitespace, align, overflowWrap, width))
    }

    fun println(
            message: Any?,
            style: TextStyle = DEFAULT_STYLE,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ) {
        rawPrintln(render(message, style, whitespace, align, overflowWrap, width))
    }

    fun print(renderable: Renderable) {
        rawPrint(render(renderable))
    }

    fun println(renderable: Renderable) {
        rawPrintln(render(renderable))
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

    fun renderDanger(
            message: Any?,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ): String {
        return render(message, theme.danger, whitespace, align, overflowWrap, width)
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

    fun renderMuted(
            message: Any?,
            whitespace: Whitespace = Whitespace.PRE,
            align: TextAlign = TextAlign.NONE,
            overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
            width: Int? = null
    ): String {
        return render(message, theme.muted, whitespace, align, overflowWrap, width)
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
        return render(renderable.render(this))
    }

    fun render(lines: Lines): String
    fun println()
    fun rawPrintln(message: String)
    fun rawPrint(message: String)
}

@Suppress("FunctionName")
fun Terminal(
        ansiLevel: AnsiLevel? = null,
        theme: Theme = DEFAULT_THEME,
        width: Int? = null,
        height: Int? = null,
        hyperlinks: Boolean? = null,
        tabWidth: Int = 8
): Terminal {
    return AnsiTerminal(ansiLevel, theme, width, height, hyperlinks, tabWidth)
}
