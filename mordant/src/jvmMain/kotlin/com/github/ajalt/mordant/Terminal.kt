package com.github.ajalt.mordant

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.internal.renderLinesAnsi
import com.github.ajalt.mordant.rendering.markdown.MarkdownRenderer
import com.github.ajalt.mordant.terminal.TerminalDetection
import com.github.ajalt.mordant.terminal.TerminalInfo

class Terminal(
        ansiLevel: AnsiLevel? = null,
        val theme: Theme = DEFAULT_THEME,
        width: Int? = null,
        hyperlinks: Boolean? = null,
        val tabWidth: Int = 8
) {
    init {
        require(tabWidth >= 0) { "tab width cannot be negative" }
    }

    val info: TerminalInfo = TerminalDetection.detectTerminal(ansiLevel, width, hyperlinks)
    val colors: TerminalColors = TerminalColors(info.ansiLevel)

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
        return renderLinesAnsi(renderable.render(this), info.ansiLevel)
    }
}
