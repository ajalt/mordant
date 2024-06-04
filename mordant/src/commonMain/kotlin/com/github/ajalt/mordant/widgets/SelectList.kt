package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.table.verticalLayout
import com.github.ajalt.mordant.terminal.Terminal

/**
 * A list widget with selectable items.
 */
class SelectList(
    private val entries: List<Entry>,
    private val title: Widget? = null,
    private val cursorIndex: Int = -1, // -1 for no cursor
    private val cursorMarker: String = "❯", // style for color
    private val selectedMarker: String = "✓", // may be empty
    private val unselectedMarker: String = "•", // may be empty
    private val captionBottom: Widget? = null,
    private val selectedStyle: TextStyle = TextStyle(), // TODO: theme?
    private val unselectedTitleStyle: TextStyle = TextStyle(), // TODO: theme?
    private val unselectedMarkerStyle: TextStyle = TextStyle(), // TODO: theme?
    ) : Widget {
    class Entry(
        val title: String,
        val description: Widget? = null,
        val selected: Boolean = false,
    ) {
        constructor(title: String, description: String?, selected: Boolean = false)
                : this(title, description?.let(::Text), selected)
    }

    private val layout = table {
        title?.let(::captionTop)
        captionBottom?.let(::captionBottom)
        cellBorders = Borders.LEFT_RIGHT
        tableBorders = Borders.NONE
        borderType = BorderType.BLANK
        padding = Padding(0)
        val cursorBlank = " ".repeat(Span.word(cursorMarker.replace(" ", ".")).cellWidth)
        val styledSelectedMarker = selectedStyle(selectedMarker)
        val styledUnselectedMarker = unselectedMarkerStyle(unselectedMarker)
        body {
            for ((i, entry) in entries.withIndex()) {
                row {
                    if (cursorIndex != null) {
                        cell(if (i == cursorIndex) cursorMarker else cursorBlank)
                    }
                    if (selectedMarker.isNotEmpty()) {
                        cell(if (entry.selected) styledSelectedMarker else styledUnselectedMarker)
                    }
                    val title = when {
                        entry.selected -> selectedStyle(entry.title)
                        else -> unselectedTitleStyle(entry.title)
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

    override fun measure(t: Terminal, width: Int): WidthRange = layout.measure(t, width)
    override fun render(t: Terminal, width: Int): Lines = layout.render(t, width)
}
