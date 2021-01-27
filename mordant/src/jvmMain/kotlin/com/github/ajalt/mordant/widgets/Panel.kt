package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.ThemeDimension
import com.github.ajalt.mordant.internal.ThemeString
import com.github.ajalt.mordant.internal.ThemeStyle
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.OverflowWrap.ELLIPSES
import com.github.ajalt.mordant.rendering.TextAlign.CENTER
import com.github.ajalt.mordant.rendering.TextAlign.LEFT
import com.github.ajalt.mordant.rendering.Whitespace.NOWRAP
import com.github.ajalt.mordant.terminal.Terminal

private val DEFAULT_PADDING = Padding.none()
private fun titleWidget(title: String?, titleTextStyle: TextStyle?): Text? {
    return title?.let {
        Text(it, titleTextStyle ?: DEFAULT_STYLE, overflowWrap = ELLIPSES, whitespace = NOWRAP)
    }
}


class Panel private constructor(
    content: Widget,
    private val title: Widget?,
    private val expand: Boolean,
    padding: Padding,
    private val borderStyle: BorderStyle?,
    private val titleAlign: TextAlign,
    private val borderTextStyle: ThemeStyle,
    private val titleTextStyle: ThemeStyle?,
    private val titlePadding: ThemeDimension,
) : Widget {
    constructor(
        content: Widget,
        title: Widget? = null,
        expand: Boolean = false,
        padding: Padding = DEFAULT_PADDING,
        borderStyle: BorderStyle? = BorderStyle.ROUNDED,
        titleAlign: TextAlign = CENTER,
        borderTextStyle: TextStyle? = null,
        titlePadding: Int? = null,
    ) : this(
        content = content,
        title = title,
        expand = expand,
        padding = padding,
        borderStyle = borderStyle,
        titleAlign = titleAlign,
        borderTextStyle = ThemeStyle.of("panel.border", borderTextStyle),
        titleTextStyle = null,
        titlePadding = ThemeDimension.of("panel.title.padding", titlePadding),
    )

    constructor(
        content: String,
        title: String? = null,
        expand: Boolean = false,
        padding: Padding = DEFAULT_PADDING,
        borderStyle: BorderStyle? = BorderStyle.ROUNDED,
        titleAlign: TextAlign = CENTER,
        borderTextStyle: TextStyle? = null,
        titleTextStyle: TextStyle? = borderTextStyle,
        titlePadding: Int? = null,
    ) : this(
        content = Text(content),
        title = titleWidget(title, titleTextStyle),
        expand = expand,
        padding = padding,
        borderStyle = borderStyle,
        titleAlign = titleAlign,
        borderTextStyle = ThemeStyle.of("panel.border", borderTextStyle),
        // The explicit style is baked in to the Text object, so only override the rendered style if
        // we need it from the theme.
        titleTextStyle = ThemeStyle.of("panel.title", null).takeIf { titleTextStyle == null },
        titlePadding = ThemeDimension.of("panel.title.padding", titlePadding),
    )

    private val content: Widget = content.withPadding(padding)
    private val borderWidth get() = if (borderStyle == null) 0 else 2

    private fun maxContentWidth(width: Int) = (width - borderWidth).coerceAtLeast(0)
    private fun maxTitleWidth(titlePadding: Int, width: Int): Int {
        return (maxContentWidth(width) - titlePadding * 2).coerceAtLeast(0)
    }

    override fun measure(t: Terminal, width: Int): WidthRange {
        val contentWidth = content.measure(t, maxContentWidth(width)) + borderWidth
        val titlePadding = titlePadding[t.theme]
        val titleWidth = title?.measure(t, maxTitleWidth(width, titlePadding))?.plus(borderWidth + titlePadding * 2)

        return listOf(
            if (expand) contentWidth.copy(min = contentWidth.max) else contentWidth,
            titleWidth
        ).maxWidthRange { it }
    }

    override fun render(t: Terminal, width: Int): Lines {
        val measurement = measure(t, width)
        val maxContentWidth = maxContentWidth(width)
        val borderTextStyle = borderTextStyle[t.theme]

        val contentWidth = when {
            expand -> maxContentWidth
            else -> (measurement.max - borderWidth).coerceAtMost(maxContentWidth)
        }

        val renderedContent = content.render(t, maxContentWidth).setSize(contentWidth, textAlign = LEFT)
        val renderedTitle = HorizontalRule(
            title ?: EmptyWidget,
            ThemeString.Explicit(borderStyle?.body?.ew ?: " "),
            this.borderTextStyle,
            this.titleTextStyle,
            titleAlign,
            titlePadding
        ).render(t, contentWidth)

        if (borderStyle == null) {
            return when (title) {
                null -> renderedContent
                else -> Lines(renderedTitle.lines + renderedContent.lines)
            }
        }

        val lines = ArrayList<Line>(renderedContent.height + renderedTitle.height + borderWidth)
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
