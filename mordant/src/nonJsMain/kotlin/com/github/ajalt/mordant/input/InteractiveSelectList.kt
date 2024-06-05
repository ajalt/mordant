package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.animation.animation
import com.github.ajalt.mordant.rendering.TextColors.brightWhite
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.SelectList
import com.github.ajalt.mordant.widgets.Text
import kotlin.jvm.JvmName

/**
 * Display a list of items and allow the user to select one with the arrow keys and enter.
 *
 * @return the selected item title, or null if the user canceled the selection
 */
inline fun Terminal.interactiveSelectList(
    block: InteractiveSelectListBuilder.() -> Unit,
): String? {
    return InteractiveSelectListBuilder(this).apply(block).runSingleSelect()
}

/**
 * Display a list of items and allow the user to select one with the arrow keys and enter.
 *
 * @param entries The list of items to select from
 * @param title The title to display above the list
 * @return the selected item title, or null if the user canceled the selection
 */
@JvmName("interactiveSelectListString")
fun Terminal.interactiveSelectList(
    entries: List<String>,
    title: String = "",
): String? {
    return interactiveSelectList {
        entries(entries)
        title(title)
    }
}

/**
 * Display a list of items and allow the user to select one with the arrow keys and enter.
 *
 * @param entries The list of items to select from
 * @param title The title to display above the list
 * @return the selected item title, or null if the user canceled the selection
 */
@JvmName("interactiveSelectListEntry")
fun Terminal.interactiveSelectList(
    entries: List<SelectList.Entry>,
    title: String = "",
): String? {
    return interactiveSelectList {
        entries(entries)
        title(title)
    }
}

/**
 * Display a list of items and allow the user to select zero or more with the arrow keys and enter.
 *
 * @return the selected item titles, or null if the user canceled the selection
 */
inline fun Terminal.interactiveMultiSelectList(
    block: InteractiveSelectListBuilder.() -> Unit,
): List<String>? {
    return InteractiveSelectListBuilder(this).apply(block).runMultiSelect()
}

/**
 * Display a list of items and allow the user to select zero or more with the arrow keys and enter.
 *
 * @param entries The list of items to select from
 * @param title The title to display above the list
 * @return the selected item titles, or null if the user canceled the selection
 */
@JvmName("interactiveMultiSelectListEntry")
fun Terminal.interactiveMultiSelectList(
    entries: List<SelectList.Entry>,
    title: String = "",
): List<String>? {
    return interactiveMultiSelectList {
        entries(entries)
        title(title)
    }
}

/**
 * Display a list of items and allow the user to select zero or more with the arrow keys and enter.
 *
 * @param entries The list of items to select from
 * @param title The title to display above the list
 * @return the selected item titles, or null if the user canceled the selection
 */
@JvmName("interactiveMultiSelectListString")
fun Terminal.interactiveMultiSelectList(
    entries: List<String>,
    title: String = "",
): List<String>? {
    return interactiveMultiSelectList {
        entries(entries)
        title(title)
    }
}

/**
 * Configure an interactive selection list.
 *
 * Run the selection with [runSingleSelect] or [runMultiSelect].
 */
class InteractiveSelectListBuilder(private val terminal: Terminal) {
    private var entries: MutableList<SelectList.Entry> = mutableListOf()
    private var title: Widget? = null
    private var limit: Int = Int.MAX_VALUE
    private var startingCursorIndex: Int = 0
    private var onlyShowActiveDescription: Boolean = false
    private var clearOnExit: Boolean = true
    private var cursorMarker: String? = null
    private var selectedMarker: String? = null
    private var unselectedMarker: String? = null
    private var selectedStyle: TextStyle? = null
    private var unselectedTitleStyle: TextStyle? = null
    private var unselectedMarkerStyle: TextStyle? = null
    private var keyNext: KeyboardEvent = KeyboardEvent("ArrowDown")
    private var keyPrev: KeyboardEvent = KeyboardEvent("ArrowUp")
    private var keySubmit: KeyboardEvent = KeyboardEvent("Enter")
    private var keyToggle: KeyboardEvent = KeyboardEvent("x")
    private var instructions: Widget? = null

    /** Set the list of items to select from */
    fun entries(vararg entries: SelectList.Entry): InteractiveSelectListBuilder = apply {
        entries(entries.toList())
    }

    /** Set the list of items to select from */
    @JvmName("entriesEntry")
    fun entries(entries: List<SelectList.Entry>): InteractiveSelectListBuilder = apply {
        this.entries = entries.toMutableList()
    }

    /** Set the list of items to select from */
    fun entries(vararg entries: String): InteractiveSelectListBuilder = apply {
        entries(entries.toList())
    }

    /** Set the list of items to select from */
    @JvmName("entriesString")
    fun entries(entries: List<String>): InteractiveSelectListBuilder = apply {
        this.entries = entries.mapTo(mutableListOf()) { SelectList.Entry(it) }
    }

    /** Add an item to the list of items to select from */
    fun addEntry(entry: SelectList.Entry): InteractiveSelectListBuilder = apply {
        this.entries += entry
    }

    /** Add an item to the list of items to select from */
    fun addEntry(entry: String): InteractiveSelectListBuilder = apply {
        this.entries += SelectList.Entry(entry)
    }

    /** Set the title to display above the list */
    fun title(title: String): InteractiveSelectListBuilder = apply {
        this.title = when {
            title.isEmpty() -> null
            else -> Text(terminal.theme.style("select.title")(title))
        }
    }

    /** Set the title to display above the list */
    fun title(title: Widget): InteractiveSelectListBuilder = apply {
        this.title = title
    }

    /** Set the maximum number of items that can be selected */
    fun limit(limit: Int): InteractiveSelectListBuilder = apply {
        this.limit = limit
    }

    /** Set the index of the item to start the cursor on */
    fun startingCursorIndex(startingCursorIndex: Int): InteractiveSelectListBuilder = apply {
        this.startingCursorIndex = startingCursorIndex
    }

    /** If true, only show the description of the highlighted item */
    fun onlyShowActiveDescription(onlyShowActiveDescription: Boolean): InteractiveSelectListBuilder {
        return apply { this.onlyShowActiveDescription = onlyShowActiveDescription }
    }

    /** If true, clear the list when the user submits the selections. If false, leave it on screen */
    fun clearOnExit(clearOnExit: Boolean): InteractiveSelectListBuilder = apply {
        this.clearOnExit = clearOnExit
    }

    /**
     * Set the marker to display where the cursor is located. Defaults to the theme string
     * `select.cursor`
     */
    fun cursorMarker(cursorMarker: String): InteractiveSelectListBuilder = apply {
        this.cursorMarker = cursorMarker
    }

    /**
     * Set the marker to display for selected items. Defaults to the theme string
     * `select.selected`
     */
    fun selectedMarker(selectedMarker: String): InteractiveSelectListBuilder = apply {
        this.selectedMarker = selectedMarker
    }

    /**
     * Set the marker to display for unselected items. Defaults to the theme string
     * `select.unselected`
     */
    fun unselectedMarker(unselectedMarker: String): InteractiveSelectListBuilder = apply {
        this.unselectedMarker = unselectedMarker
    }

    /**
     * Set the style to use to highlight the currently selected item. Defaults to the theme style
     * `select.selected`
     */
    fun selectedStyle(selectedStyle: TextStyle): InteractiveSelectListBuilder = apply {
        this.selectedStyle = selectedStyle
    }

    /**
     * Set the style to use for the title of unselected items. Defaults to the theme style
     * `select.unselected-title`
     */
    fun unselectedTitleStyle(unselectedTitleStyle: TextStyle): InteractiveSelectListBuilder {
        return apply { this.unselectedTitleStyle = unselectedTitleStyle }
    }

    /**
     * Set the style to use for the marker of unselected items. Defaults to the theme style
     * `select.unselected-marker`
     */
    fun unselectedMarkerStyle(unselectedMarkerStyle: TextStyle): InteractiveSelectListBuilder {
        return apply { this.unselectedMarkerStyle = unselectedMarkerStyle }
    }

    /** Set the key to move the cursor down */
    fun keyNext(keyNext: KeyboardEvent): InteractiveSelectListBuilder = apply {
        this.keyNext = keyNext
    }

    /** Set the key to move the cursor up */
    fun keyPrev(keyPrev: KeyboardEvent): InteractiveSelectListBuilder = apply {
        this.keyPrev = keyPrev
    }

    /** Set the key to submit the selection */
    fun keySubmit(keySubmit: KeyboardEvent): InteractiveSelectListBuilder = apply {
        this.keySubmit = keySubmit
    }

    /** Set the key to toggle the selection of an item */
    fun keyToggle(keyToggle: KeyboardEvent): InteractiveSelectListBuilder = apply {
        this.keyToggle = keyToggle
    }

    /** Set the instructions to display at the bottom of the list */
    fun instructions(instructions: Widget): InteractiveSelectListBuilder = apply {
        this.instructions = instructions
    }

    /** Set the instructions to display at the bottom of the list */
    fun instructions(instructions: String): InteractiveSelectListBuilder = apply {
        this.instructions = Text(instructions)
    }

    /**
     * Run the select list in single select mode and return the selected item title, or `null`
     * if the user canceled the selection
     */
    fun runSingleSelect(): String? {
        return runAnimation(singleSelect = true)?.first()?.title
    }

    /**
     * Run the select list in multi select mode and return the selected item titles, or `null` if the
     * user canceled the selection
     */
    fun runMultiSelect(): List<String>? {
        return runAnimation(singleSelect = false)
            ?.mapNotNull { if (it.selected) it.title else null }
    }

    private fun runAnimation(singleSelect: Boolean): List<SelectList.Entry>? {
        require(entries.isNotEmpty()) { "Select list must have at least one entry" }

        val items = entries.toMutableList()
        val caption = instructions?.let {
            val s = TextStyle(brightWhite, bold = true)
            val text = when {
                singleSelect -> {
                    " ${s("↑")} up • ${s("↓")} down • ${s("enter")} select"
                }

                else -> {
                    " ${s("x")} toggle • ${s("↑")} up • ${s("↓")} down • ${s("enter")} confirm"
                }
            }
            Text(dim(text))
        }

        terminal.enterRawMode()?.use { scope ->
            val a = terminal.animation<Int> { i ->
                SelectList(
                    items,
                    title = title,
                    cursorIndex = i,
                    styleOnHover = singleSelect,
                    selectedMarker = selectedMarker,
                    cursorMarker = cursorMarker,
                    unselectedMarker = unselectedMarker,
                    selectedStyle = selectedStyle,
                    unselectedTitleStyle = unselectedTitleStyle,
                    unselectedMarkerStyle = unselectedMarkerStyle,
                    captionBottom = caption,
                )
            }

            try {
                var cursor = startingCursorIndex
                fun updateCursor(newCursor: Int) {
                    cursor = newCursor.coerceIn(0, entries.lastIndex)
                    if (onlyShowActiveDescription) {
                        items.forEachIndexed { i, entry ->
                            items[i] = entry.copy(
                                description = if (i == cursor) entries[i].description else null
                            )
                        }
                    }
                }
                while (true) {
                    a.update(cursor)
                    val key = scope.readKey()
                    val entry = items[cursor]
                    when {
                        key == null -> return null
                        key.isCtrlC() -> return null
                        key == keyPrev -> updateCursor(cursor - 1)
                        key == keyNext -> updateCursor(cursor + 1)

                        !singleSelect && key == keyToggle -> {
                            if (entry.selected || items.count { it.selected } < limit) {
                                items[cursor] = entry.copy(selected = !entry.selected)
                            }
                        }

                        key == keySubmit -> {
                            if (singleSelect) return listOf(entry)
                            return items
                        }
                    }
                }
            } finally {
                if (clearOnExit) a.clear() else a.stop()
            }
        }
        return null
    }
}
