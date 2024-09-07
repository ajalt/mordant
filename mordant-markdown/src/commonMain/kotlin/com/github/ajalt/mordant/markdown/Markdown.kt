package com.github.ajalt.mordant.markdown

import com.github.ajalt.mordant.rendering.Lines
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.rendering.WidthRange
import com.github.ajalt.mordant.terminal.Terminal

/**
 * A [Widget] that renders a GitHub Flavored [markdown] string.
 *
 * @property showHtml If `true`, any html tags in the [markdown] will be rendered verbatim to the
 *   output. By default, html tags are skipped.
 * @property hyperlinks If `true`, links will always be rendered with ANSI hyperlinks. If `false`,
 *   links will always print their targets instead. By default, hyperlinks are used if the current
 *   terminal supports them.
 */
class Markdown(
    private val markdown: String,
    private val showHtml: Boolean = false,
    private val hyperlinks: Boolean? = null,
) : Widget {
    private var document: Widget? = null
    private fun document(t: Terminal): Widget {
        if (document == null) {
            document = MarkdownRenderer(markdown, t.theme, showHtml, hyperlinks ?: t.terminalInfo.ansiHyperLinks).render()
        }
        return document!!
    }

    override fun measure(t: Terminal, width: Int): WidthRange = document(t).measure(t, width)
    override fun render(t: Terminal, width: Int): Lines = document(t).render(t, width)
}
