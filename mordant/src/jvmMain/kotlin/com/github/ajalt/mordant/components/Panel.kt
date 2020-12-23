package com.github.ajalt.mordant.components

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.OverflowWrap.ELLIPSES
import com.github.ajalt.mordant.rendering.TextAlign.CENTER
import com.github.ajalt.mordant.rendering.TextAlign.LEFT
import com.github.ajalt.mordant.rendering.Whitespace.NOWRAP
import com.github.ajalt.mordant.terminal.Terminal

private val DEFAULT_PADDING = Padding.none()
private fun titleRenderable(title: String?, titleTextStyle: TextStyle): Text? {
    return title?.let { Text(it, titleTextStyle, overflowWrap = ELLIPSES, whitespace = NOWRAP) }
}

// TODO: theme
class Panel(
    content: Renderable,
    private val title: Renderable? = null,
    private val expand: Boolean = false,
    padding: Padding = DEFAULT_PADDING,
    private val borderStyle: BorderStyle? = BorderStyle.ROUNDED,
    private val titleAlign: TextAlign = CENTER,
    private val borderTextStyle: TextStyle = DEFAULT_STYLE,
    private val titleTextStyle: TextStyle = borderTextStyle
) : Renderable {
    constructor(
        content: String,
        title: String? = null,
        expand: Boolean = false,
        padding: Padding = DEFAULT_PADDING,
        borderStyle: BorderStyle? = BorderStyle.ROUNDED,
        titleAlign: TextAlign = CENTER,
        borderTextStyle: TextStyle = DEFAULT_STYLE,
        titleTextStyle: TextStyle = borderTextStyle
    ) : this(
        Text(content),
        titleRenderable(title, titleTextStyle),
        expand,
        padding,
        borderStyle,
        titleAlign,
        borderTextStyle,
        titleTextStyle
    )

    private val content: Renderable = content.withPadding(padding)
    private val borderWidth get() = if (borderStyle == null) 0 else 2
    private val titlePadding = 2

    private fun maxContentWidth(width: Int) = (width - borderWidth).coerceAtLeast(0)
    private fun maxTitleWidth(width: Int) = (maxContentWidth(width) - titlePadding).coerceAtLeast(0)

    override fun measure(t: Terminal, width: Int): WidthRange {
        val contentWidth = content.measure(t, maxContentWidth(width)) + borderWidth
        val titleWidth = title?.measure(t, maxTitleWidth(width))?.plus(borderWidth + titlePadding)

        return listOf(
            if (expand) contentWidth.copy(min = contentWidth.max) else contentWidth,
            titleWidth
        ).maxWidthRange { it }
    }

    override fun render(t: Terminal, width: Int): Lines {
        val measurement = measure(t, width)
        val maxContentWidth = maxContentWidth(width)

        val contentWidth = when {
            expand -> maxContentWidth
            else -> measurement.max.coerceAtMost(maxContentWidth) - borderWidth
        }

        val renderedContent = content.render(t, maxContentWidth).setSize(contentWidth, textAlign = LEFT)
        val renderedTitle = HorizontalRule(
            title ?: EmptyRenderable,
            borderStyle?.body?.ew ?: " ",
            borderTextStyle,
            titleTextStyle,
            titleAlign
        ).render(t, contentWidth)

        if (borderStyle == null) {
            if (title == null) return renderedContent
            return Lines(renderedTitle.lines + renderedContent.lines)
        }

        val lines = ArrayList<Line>(renderedContent.lines.size + borderWidth)
        val b = borderStyle.body
        val horizontalBorder = Span.word(b.ew.repeat(contentWidth), borderTextStyle)

        renderedTitle.lines.mapIndexedTo(lines) { i, it ->
            if (i < renderedTitle.lines.lastIndex) it
            else flatLine(Span.word(b.es, borderTextStyle), it, Span.word(b.sw, borderTextStyle))
        }

        val vertical = listOf(Span.word(b.ns, borderTextStyle))

        renderedContent.lines.mapTo(lines) { line ->
            flatLine(vertical, line, vertical)
        }

        lines.add(listOf(Span.word(b.ne, borderTextStyle), horizontalBorder, Span.word(b.nw, borderTextStyle)))
        return Lines(lines)
    }
}
