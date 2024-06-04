package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.MppAtomicRef
import com.github.ajalt.mordant.internal.ThemeString
import com.github.ajalt.mordant.internal.ThemeStyle
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.table.verticalLayout
import com.github.ajalt.mordant.terminal.Terminal

/**
 * A list widget with selectable items.
 */
class SelectList private constructor(
    private val entries: List<Entry>,
    private val title: Widget?,
    private val cursorIndex: Int?,
    private val styleOnHover: Boolean ,
    private val cursorMarker: ThemeString,
    private val selectedMarker: ThemeString,
    private val unselectedMarker: ThemeString,
    private val captionBottom: Widget?,
    private val cursorStyle: ThemeStyle,
    private val selectedStyle: ThemeStyle,
    private val unselectedTitleStyle: ThemeStyle,
    private val unselectedMarkerStyle: ThemeStyle,
) : Widget {
    constructor(
        entries: List<Entry>,
        title: Widget? = null,
        cursorIndex: Int = -1, // -1 for no cursor
        styleOnHover: Boolean = false,
        cursorMarker: String? = null,
        selectedMarker: String? = null,
        unselectedMarker: String? = null,
        captionBottom: Widget? = null,
        selectedStyle: TextStyle? = null,
        cursorStyle: TextStyle? = null,
        unselectedTitleStyle: TextStyle? = null,
        unselectedMarkerStyle: TextStyle? = null,
    ) : this(
        entries = entries,
        title = title,
        cursorIndex = cursorIndex,
        styleOnHover = styleOnHover,
        cursorMarker = ThemeString.of("select.cursor", cursorMarker, "❯"),
        selectedMarker = ThemeString.of("select.selected", selectedMarker, "✓"),
        unselectedMarker = ThemeString.of("select.unselected", unselectedMarker, "•"),
        captionBottom = captionBottom,
        cursorStyle = ThemeStyle.of("select.cursor", selectedStyle),
        selectedStyle = ThemeStyle.of("select.selected", cursorStyle),
        unselectedTitleStyle = ThemeStyle.of("select.unselected-title", unselectedTitleStyle),
        unselectedMarkerStyle = ThemeStyle.of("select.unselected-marker", unselectedMarkerStyle),
    )

    // TODO: docs
    data class Entry(
        val title: String,
        val description: Widget? = null,
        val selected: Boolean = false,
    ) {
        constructor(title: String, description: String?, selected: Boolean = false)
                : this(title, description?.let(::Text), selected)
    }

    private val widget: MppAtomicRef<Widget?> = MppAtomicRef(null)
    private fun layout(t: Terminal): Widget {
        widget.value?.let { return it }
        val w = table {
            title?.let(::captionTop)
            captionBottom?.let(::captionBottom)
            cellBorders = Borders.LEFT_RIGHT
            tableBorders = Borders.NONE
            borderType = BorderType.BLANK
            padding = Padding(0)
            val cursorBlank = " ".repeat(Span.word(cursorMarker[t].replace(" ", ".")).cellWidth)
            val cursor = cursorStyle[t](cursorMarker[t])
            val styledSelectedMarker = selectedStyle[t](selectedMarker[t])
            val styledUnselectedMarker = unselectedMarkerStyle[t](unselectedMarker[t])
            body {
                for ((i, entry) in entries.withIndex()) {
                    row {
                        if (cursorIndex != null) {
                            cell(if (i == cursorIndex) cursor else cursorBlank)
                        }
                        if (selectedMarker[t].isNotEmpty()) {
                            cell(if (entry.selected) styledSelectedMarker else styledUnselectedMarker)
                        }
                        val title = when {
                            entry.selected -> selectedStyle[t](entry.title)
                            i == cursorIndex && styleOnHover -> selectedStyle[t](entry.title)
                            else -> unselectedTitleStyle[t](entry.title)
                        }
                        cell(when {
                            entry.description != null -> verticalLayout {
                                cells(title, entry.description)
                            }

                            else -> Text(title)
                        })
                    }
                }
            }
        }
        if (widget.compareAndSet(null, w)) return w
        return widget.value!!
    }

    override fun measure(t: Terminal, width: Int): WidthRange = layout(t).measure(t, width)
    override fun render(t: Terminal, width: Int): Lines = layout(t).render(t, width)
}
