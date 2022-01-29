package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.DEFAULT_PADDING
import com.github.ajalt.mordant.internal.ThemeDimension
import com.github.ajalt.mordant.internal.ThemeString
import com.github.ajalt.mordant.internal.ThemeStyle
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.OverflowWrap.ELLIPSES
import com.github.ajalt.mordant.rendering.TextAlign.CENTER
import com.github.ajalt.mordant.rendering.TextAlign.LEFT
import com.github.ajalt.mordant.rendering.Whitespace.NOWRAP
import com.github.ajalt.mordant.terminal.Terminal


class Panel private constructor(
    content: Widget,
    private val title: Widget?,
    private val expand: Boolean,
    padding: Padding,
    private val borderType: BorderType?,
    private val titleAlign: TextAlign,
    private val borderStyle: ThemeStyle,
    private val titlePadding: ThemeDimension,
) : Widget {
    constructor(
        content: Widget,
        title: Widget? = null,
        expand: Boolean = false,
        padding: Padding = DEFAULT_PADDING,
        borderType: BorderType? = BorderType.ROUNDED,
        titleAlign: TextAlign = CENTER,
        borderStyle: TextStyle? = null,
        titlePadding: Int? = null,
    ) : this(
        content = content,
        title = title,
        expand = expand,
        padding = padding,
        borderType = borderType,
        titleAlign = titleAlign,
        borderStyle = ThemeStyle.of("panel.border", borderStyle),
        titlePadding = ThemeDimension.of("panel.title.padding", titlePadding),
    )

    constructor(
        content: String,
        title: String? = null,
        expand: Boolean = false,
        padding: Padding = DEFAULT_PADDING,
        borderType: BorderType? = BorderType.ROUNDED,
        titleAlign: TextAlign = CENTER,
        borderStyle: TextStyle? = null,
        titlePadding: Int? = null,
    ) : this(
        content = Text(content),
        title = title?.let { Text(it, overflowWrap = ELLIPSES, whitespace = NOWRAP) },
        expand = expand,
        padding = padding,
        borderType = borderType,
        titleAlign = titleAlign,
        borderStyle = ThemeStyle.of("panel.border", borderStyle),
        titlePadding = ThemeDimension.of("panel.title.padding", titlePadding),
    )

    private val content: Widget = content.withPadding(padding)
    private val borderWidth get() = if (borderType == null) 0 else 2

    private fun maxContentWidth(width: Int) = (width - borderWidth).coerceAtLeast(0)
    private fun maxTitleWidth(titlePadding: Int, width: Int): Int {
        return (maxContentWidth(width) - titlePadding * 2).coerceAtLeast(0)
    }

    override fun measure(t: Terminal, width: Int): WidthRange {
        val contentWidth = content.measure(t, maxContentWidth(width)) + borderWidth
        val titlePadding = titlePadding[t]
        val titleWidth = title?.measure(t, maxTitleWidth(width, titlePadding))?.plus(borderWidth + titlePadding * 2)

        return listOf(
            if (expand) contentWidth.copy(min = contentWidth.max) else contentWidth,
            titleWidth
        ).maxWidthRange { it }
    }

    override fun render(t: Terminal, width: Int): Lines {
        val measurement = measure(t, width)
        val maxContentWidth = maxContentWidth(width)
        val borderStyle = borderStyle[t]

        val contentWidth = when {
            expand -> maxContentWidth
            else -> (measurement.max - borderWidth).coerceAtMost(maxContentWidth)
        }

        val renderedContent = content.render(t, maxContentWidth).setSize(contentWidth, textAlign = LEFT)
        val renderedTitle = HorizontalRule(
            title ?: EmptyWidget,
            ThemeString.Explicit(borderType?.body?.ew ?: " "),
            this.borderStyle,
            titleAlign,
            titlePadding
        ).render(t, contentWidth)

        if (borderType == null) {
            return when (title) {
                null -> renderedContent
                else -> Lines(renderedTitle.lines + renderedContent.lines)
            }
        }

        val lines = ArrayList<Line>(renderedContent.height + renderedTitle.height + borderWidth)
        val b = borderType.body
        val horizontalBorder = Span.word(b.ew.repeat(contentWidth), borderStyle)

        renderedTitle.lines.mapIndexedTo(lines) { i, it ->
            if (i < renderedTitle.lines.lastIndex) it
            else flatLine(Span.word(b.es, borderStyle), it, Span.word(b.sw, borderStyle))
        }

        val vertical = listOf(Span.word(b.ns, borderStyle))

        renderedContent.lines.mapTo(lines) { line ->
            flatLine(vertical, line, vertical)
        }

        lines += Line(listOf(Span.word(b.ne, borderStyle), horizontalBorder, Span.word(b.nw, borderStyle)))
        return Lines(lines)
    }
}
