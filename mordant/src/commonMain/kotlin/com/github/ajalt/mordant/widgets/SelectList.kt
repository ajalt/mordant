package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.MppAtomicRef
import com.github.ajalt.mordant.internal.ThemeString
import com.github.ajalt.mordant.internal.ThemeStyle
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.table.verticalLayout
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.jvm.JvmOverloads

/**
 * A list widget with selectable items.
 *
 * Use `interactiveSelectList` to create a list that can be interacted with.
 */
class SelectList private constructor(
    private val entries: List<Entry>,
    private val title: Widget?,
    private val cursorIndex: Int?,
    private val styleOnHover: Boolean,
    private val cursorMarker: ThemeString,
    private val selectedMarker: ThemeString,
    private val unselectedMarker: ThemeString,
    private val captionBottom: Widget?,
    private val selectedStyle: ThemeStyle,
    private val cursorStyle: ThemeStyle,
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
        selectedStyle = ThemeStyle.of("select.selected", selectedStyle),
        cursorStyle = ThemeStyle.of("select.cursor", cursorStyle),
        unselectedTitleStyle = ThemeStyle.of("select.unselected-title", unselectedTitleStyle),
        unselectedMarkerStyle = ThemeStyle.of("select.unselected-marker", unselectedMarkerStyle),
    )

    data class Entry @JvmOverloads constructor(
        /** The title of the entry. */
        val title: String,
        /** An optional description of the entry. */
        val description: Widget? = null,
        /** Whether this entry is marked as selected. */
        val selected: Boolean = false,
        /** Return this value instead of title if not null. */
        val value: String? = null,
    ) {
        @JvmOverloads
        constructor(title: String, description: String?, selected: Boolean = false, value: String? = null)
                : this(
            title = title,
            description = description?.let { Text(it, whitespace = Whitespace.PRE_WRAP) },
            selected = selected,
            value = value
        )
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
            whitespace = Whitespace.PRE_WRAP
            val cursorBlank = when {
                cursorMarker[t].isEmpty() -> ""
                else -> " ".repeat(Span.word(cursorMarker[t].replace(" ", ".")).cellWidth)
            }
            val cursor = cursorStyle[t](cursorMarker[t])
            val styledSelectedMarker = selectedStyle[t](selectedMarker[t])
            val styledUnselectedMarker = unselectedMarkerStyle[t](unselectedMarker[t])
            body {
                for ((i, entry) in entries.withIndex()) {
                    row {
                        if (cursorIndex != null && cursorBlank.isNotEmpty()) {
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
                                whitespace = Whitespace.PRE_WRAP
                                cells(title, entry.description)
                            }

                            else -> title
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
