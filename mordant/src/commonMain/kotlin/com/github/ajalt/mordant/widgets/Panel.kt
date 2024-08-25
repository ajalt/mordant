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


/**
 * A box drawn around another widget, with optional top and bottom titles.
 *
 * ### Example
 *
 * ```
 * terminal.print(Panel(
 *   "my panel content",
 *   title = "title",
 *   bottomTitle = "subtitle",
 *   titleAlign = TextAlign.LEFT,
 *   bottomTitleAlign = TextAlign.RIGHT
 * ))
 * ```
 *
 * ```
 * ╭─ title ────────╮
 * │my panel content│
 * ╰───── subtitle ─╯
 * ```
 */
class Panel private constructor(
    content: Widget,
    private val title: Widget?,
    private val bottomTitle: Widget?,
    private val expand: Boolean,
    padding: Padding,
    private val borderType: BorderType?,
    private val titleAlign: TextAlign,
    private val bottomTitleAlign: TextAlign,
    private val borderStyle: ThemeStyle,
    private val titlePadding: ThemeDimension,
) : Widget {
    /**
     * @param content The content to draw in the center of the panel.
     * @param title The content to draw in the top border
     * @param bottomTitle The content to draw in the bottom border
     * @param expand If true, the panel will expand horizontally  to fill the screen. If false, it will be just wide enough to fit the content.
     * @param padding Extra padding to add between the content and the panel border. (default: no extra padding)
     * @param borderType The type of border to draw around the content, or `null` for no border
     * @param titleAlign The horizontal alignment for the top title (default: center)
     * @param bottomTitleAlign The horizontal alignment for the bottom title (default: copy [titleAlign])
     * @param borderStyle The [TextStyle] for the border (default: Theme style `panel.border`)
     * @param titlePadding Amount of horizontal padding to add between the titles and the border characters (default: Theme value `panel.title.padding`)
     */
    constructor(
        content: Widget,
        title: Widget? = null,
        bottomTitle: Widget? = null,
        expand: Boolean = false,
        padding: Padding = DEFAULT_PADDING,
        borderType: BorderType? = BorderType.ROUNDED,
        titleAlign: TextAlign = CENTER,
        bottomTitleAlign: TextAlign = titleAlign,
        borderStyle: TextStyle? = null,
        titlePadding: Int? = null,
    ) : this(
        content = content,
        title = title,
        bottomTitle = bottomTitle,
        expand = expand,
        padding = padding,
        borderType = borderType,
        titleAlign = titleAlign,
        bottomTitleAlign = bottomTitleAlign,
        borderStyle = ThemeStyle.of("panel.border", borderStyle),
        titlePadding = ThemeDimension.of("panel.title.padding", titlePadding),
    )

    /**
     * @param content The content to draw in the center of the panel.
     * @param title The content to draw in the top border
     * @param bottomTitle The content to draw in the bottom border
     * @param expand If true, the panel will expand horizontally  to fill the screen. If false, it will be just wide enough to fit the content.
     * @param padding Extra padding to add between the content and the panel border. (default: no extra padding)
     * @param borderType The type of border to draw around the content, or `null` for no border
     * @param titleAlign The horizontal alignment for the top title (default: center)
     * @param bottomTitleAlign The horizontal alignment for the bottom title (default: copy [titleAlign])
     * @param borderStyle The [TextStyle] for the border (default: Theme style `panel.border`)
     * @param titlePadding Amount of horizontal padding to add between the titles and the border characters (default: Theme value `panel.title.padding`)
     */
    constructor(
        content: String,
        title: String? = null,
        bottomTitle: String? = null,
        expand: Boolean = false,
        padding: Padding = DEFAULT_PADDING,
        borderType: BorderType? = BorderType.ROUNDED,
        titleAlign: TextAlign = CENTER,
        bottomTitleAlign: TextAlign = titleAlign,
        borderStyle: TextStyle? = null,
        titlePadding: Int? = null,
    ) : this(
        content = Text(content),
        title = title?.let { Text(it, overflowWrap = ELLIPSES, whitespace = NOWRAP) },
        bottomTitle = bottomTitle?.let { Text(it, overflowWrap = ELLIPSES, whitespace = NOWRAP) },
        expand = expand,
        padding = padding,
        borderType = borderType,
        titleAlign = titleAlign,
        bottomTitleAlign = bottomTitleAlign,
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
        val titleWidth = title?.measure(t, maxTitleWidth(width, titlePadding))
            ?.plus(borderWidth + titlePadding * 2)

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

        val renderedContent =
            content.render(t, maxContentWidth).setSize(contentWidth, textAlign = LEFT)
        val renderedTop = HorizontalRule(
            title ?: EmptyWidget,
            ThemeString.Explicit(borderType?.body?.ew ?: " "),
            this.borderStyle,
            titleAlign,
            titlePadding,
            titleOverflowTop = true
        ).render(t, contentWidth)

        val renderedBottom = HorizontalRule(
            bottomTitle ?: EmptyWidget,
            ThemeString.Explicit(borderType?.body?.ew ?: " "),
            this.borderStyle,
            bottomTitleAlign,
            titlePadding,
            titleOverflowTop = false
        ).render(t, contentWidth)

        if (borderType == null) {
            return Lines(buildList {
                if (title != null) addAll(renderedTop.lines)
                addAll(renderedContent.lines)
                if (bottomTitle != null) addAll(renderedBottom.lines)
            })
        }

        val lines =
            ArrayList<Line>(renderedContent.height + renderedTop.height + renderedBottom.height)
        val b = borderType.body
        val vertical = listOf(Span.word(b.ns, borderStyle))

        renderedTop.lines.mapIndexedTo(lines) { i, it ->
            if (i < renderedTop.lines.lastIndex) flatLine(Span.space(1), it, Span.space(1))
            else flatLine(Span.word(b.es, borderStyle), it, Span.word(b.sw, borderStyle))
        }

        renderedContent.lines.mapTo(lines) { line ->
            flatLine(vertical, line, vertical)
        }

        renderedBottom.lines.mapIndexedTo(lines) { i, it ->
            if (i > 0) flatLine(Span.space(1), it, Span.space(1))
            else flatLine(Span.word(b.ne, borderStyle), it, Span.word(b.nw, borderStyle))
        }

        return Lines(lines)
    }
}
