package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.animation.animation
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.SelectList
import com.github.ajalt.mordant.widgets.Text

fun Terminal.interactiveSelectList(
    items: List<String>,
    title: String = "",
    selectedMarker: String = "✓",
    cursorMarker: String = "❯ ",
    multiSelectMarker: String = "• ",
    includeInstructions: Boolean = true,// TODO includeInstructions
): String? {
    // TODO: descriptions
    val entries = items.map { SelectList.Entry(it) }
    enterRawMode()?.use { scope ->
        val a = animation<Int> { i ->
            SelectList(
                entries,
                title = if (title.isEmpty()) null else Text(title), // TODO style
                cursorIndex = i,
                selectedMarker = selectedMarker,
                cursorMarker = cursorMarker,
                unselectedMarker = multiSelectMarker
            )
        }
        try {
            var cursor = 0
            while (true) {
                a.update(cursor)
                val key = scope.readKey()
                when {
                    key == null -> return null
                    key.key == "c" && key.ctrl -> return null
                    key.key == "ArrowUp" -> cursor = (cursor - 1).coerceAtLeast(0)
                    key.key == "ArrowDown" -> cursor = (cursor + 1).coerceAtMost(entries.lastIndex)
                    key.key == "Enter" -> return items[cursor]
                }
            }
        } finally {
            a.stop()
        }
    }
    return null
}
