package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.animation.animation
import com.github.ajalt.mordant.input.InputReceiver.Status
import com.github.ajalt.mordant.internal.MppAtomicRef
import com.github.ajalt.mordant.internal.update
import com.github.ajalt.mordant.rendering.TextColors.brightWhite
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.SelectList
import com.github.ajalt.mordant.widgets.Text
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads

private class SelectConfig(
    var entries: MutableList<SelectList.Entry> = mutableListOf(),
    var title: Widget? = null,
    var limit: Int = Int.MAX_VALUE,
    var startingCursorIndex: Int = 0,
    var onlyShowActiveDescription: Boolean = false,
    var clearOnExit: Boolean = true,
    var cursorMarker: String? = null,
    var selectedMarker: String? = null,
    var unselectedMarker: String? = null,
    var selectedStyle: TextStyle? = null,
    var unselectedTitleStyle: TextStyle? = null,
    var unselectedMarkerStyle: TextStyle? = null,
    var keyNext: KeyboardEvent = KeyboardEvent("ArrowDown"),
    var descNext: String = "down",
    var keyPrev: KeyboardEvent = KeyboardEvent("ArrowUp"),
    var descPrev: String = "up",
    var keySubmit: KeyboardEvent = KeyboardEvent("Enter"),
    var descSubmit: String = "select",
    var descConfirm: String = "confirm",
    var descApplyFilter: String = "set filter",
    var keyToggle: KeyboardEvent = KeyboardEvent("x"),
    var descToggle: String = "toggle",
    var keyFilter: KeyboardEvent = KeyboardEvent("/"),
    var descFilter: String = "filter",
    var keyExitFilter: KeyboardEvent = KeyboardEvent("Escape"),
    var descExitFilter: String = "clear filter",
    var showInstructions: Boolean = true,
    var filterable: Boolean = false,
)

/**
 * Configure an interactive selection list.
 *
 * Run the selection with `runSingleSelect` or `runMultiSelect`. On JS and Wasm, where those methods
 * aren't available, you can use [createSingleSelectInputAnimation] and
 * [createMultiSelectInputAnimation] and feed them keyboard events manually.
 */
class InteractiveSelectListBuilder(private val terminal: Terminal) {
    private val config = SelectConfig()

    /** Set the list of items to select from */
    fun entries(vararg entries: SelectList.Entry): InteractiveSelectListBuilder = apply {
        entries(entries.toList())
    }

    /** Set the list of items to select from */
    @JvmName("entriesEntry")
    fun entries(entries: Iterable<SelectList.Entry>): InteractiveSelectListBuilder = apply {
        config.entries = entries.toMutableList()
    }

    /** Set the list of items to select from */
    fun entries(vararg entries: String): InteractiveSelectListBuilder = apply {
        entries(entries.toList())
    }

    /** Set the list of items to select from */
    @JvmName("entriesString")
    fun entries(entries: Iterable<String>): InteractiveSelectListBuilder = apply {
        config.entries = entries.mapTo(mutableListOf()) { SelectList.Entry(it) }
    }

    /** Add an item to the list of items to select from */
    @JvmOverloads
    fun addEntry(
        title: String,
        description: String,
        selected: Boolean = false,
        value: String? = null,
    ): InteractiveSelectListBuilder = apply {
        config.entries += SelectList.Entry(title, description, selected, value)
    }

    /** Add an item to the list of items to select from */
    @JvmOverloads
    fun addEntry(
        title: String,
        description: Widget? = null,
        selected: Boolean = false,
        value: String? = null
    ): InteractiveSelectListBuilder = apply {
        config.entries += SelectList.Entry(title, description, selected, value)
    }

    /** Add an item to the list of items to select from */
    fun addEntry(entry: SelectList.Entry): InteractiveSelectListBuilder = apply {
        config.entries += entry
    }

    /** Set the title to display above the list */
    fun title(title: String): InteractiveSelectListBuilder = apply {
        config.title = when {
            title.isEmpty() -> null
            else -> Text(terminal.theme.style("select.title")(title))
        }
    }

    /** Set the title to display above the list */
    fun title(title: Widget): InteractiveSelectListBuilder = apply {
        config.title = title
    }

    /** Set the maximum number of items that can be selected */
    fun limit(limit: Int): InteractiveSelectListBuilder = apply {
        config.limit = limit
    }

    /** Set the index of the item to start the cursor on */
    fun startingCursorIndex(startingCursorIndex: Int): InteractiveSelectListBuilder = apply {
        config.startingCursorIndex = startingCursorIndex
    }

    /** If true, only show the description of the highlighted item */
    fun onlyShowActiveDescription(onlyShowActiveDescription: Boolean): InteractiveSelectListBuilder {
        return apply { config.onlyShowActiveDescription = onlyShowActiveDescription }
    }

    /** If true, clear the list when the user submits the selections. If false, leave it on screen */
    fun clearOnExit(clearOnExit: Boolean): InteractiveSelectListBuilder = apply {
        config.clearOnExit = clearOnExit
    }

    /**
     * Set the marker to display where the cursor is located. Defaults to the theme string
     * `select.cursor`
     */
    fun cursorMarker(cursorMarker: String): InteractiveSelectListBuilder = apply {
        config.cursorMarker = cursorMarker
    }

    /**
     * Set the marker to display for selected items. Defaults to the theme string
     * `select.selected`
     */
    fun selectedMarker(selectedMarker: String): InteractiveSelectListBuilder = apply {
        config.selectedMarker = selectedMarker
    }

    /**
     * Set the marker to display for unselected items. Defaults to the theme string
     * `select.unselected`
     */
    fun unselectedMarker(unselectedMarker: String): InteractiveSelectListBuilder = apply {
        config.unselectedMarker = unselectedMarker
    }

    /**
     * Set the style to use to highlight the currently selected item. Defaults to the theme style
     * `select.selected`
     */
    fun selectedStyle(selectedStyle: TextStyle): InteractiveSelectListBuilder = apply {
        config.selectedStyle = selectedStyle
    }

    /**
     * Set the style to use for the title of unselected items. Defaults to the theme style
     * `select.unselected-title`
     */
    fun unselectedTitleStyle(unselectedTitleStyle: TextStyle): InteractiveSelectListBuilder {
        return apply { config.unselectedTitleStyle = unselectedTitleStyle }
    }

    /**
     * Set the style to use for the marker of unselected items. Defaults to the theme style
     * `select.unselected-marker`
     */
    fun unselectedMarkerStyle(unselectedMarkerStyle: TextStyle): InteractiveSelectListBuilder {
        return apply { config.unselectedMarkerStyle = unselectedMarkerStyle }
    }

    /** Set the key to move the cursor down */
    fun keyNext(keyNext: KeyboardEvent): InteractiveSelectListBuilder = apply {
        config.keyNext = keyNext
    }

    /** Set the description of the key to move the cursor down */
    fun descNext(descNext: String): InteractiveSelectListBuilder = apply {
        config.descNext = descNext
    }

    /** Set the key to move the cursor up */
    fun keyPrev(keyPrev: KeyboardEvent): InteractiveSelectListBuilder = apply {
        config.keyPrev = keyPrev
    }

    /** Set the description of the key to move the cursor up */
    fun descPrev(descPrev: String): InteractiveSelectListBuilder = apply {
        config.descPrev = descPrev
    }

    /** Set the key to submit the selection */
    fun keySubmit(keySubmit: KeyboardEvent): InteractiveSelectListBuilder = apply {
        config.keySubmit = keySubmit
    }

    /** Set the description of the key to submit the selection in single select mode */
    fun descSubmit(descSubmit: String): InteractiveSelectListBuilder = apply {
        config.descSubmit = descSubmit
    }

    /** Set the description of the key to submit the selection in multi select mode */
    fun descConfirm(descConfirm: String): InteractiveSelectListBuilder = apply {
        config.descConfirm = descConfirm
    }

    /** Set the description of the key to apply the filter when filtering in multi select mode */
    fun descApplyFilter(descApplyFilter: String): InteractiveSelectListBuilder = apply {
        config.descApplyFilter = descApplyFilter
    }

    /** Set the key to toggle the selection of an item in multi select mode */
    fun keyToggle(keyToggle: KeyboardEvent): InteractiveSelectListBuilder = apply {
        config.keyToggle = keyToggle
    }

    /** Set the description of the key to toggle the selection of an item in multi select mode */
    fun descToggle(descToggle: String): InteractiveSelectListBuilder = apply {
        config.descToggle = descToggle
    }

    /** Set the key to filter the list */
    fun keyFilter(keyFilter: KeyboardEvent): InteractiveSelectListBuilder = apply {
        config.keyFilter = keyFilter
    }

    /** Set the description of the key to filter the list */
    fun descFilter(descFilter: String): InteractiveSelectListBuilder = apply {
        config.descFilter = descFilter
    }

    /** Set the key to stop filtering the list */
    fun keyExitFilter(keyExitFilter: KeyboardEvent): InteractiveSelectListBuilder = apply {
        config.keyExitFilter = keyExitFilter
    }

    /** Set the description of the key to stop filtering the list */
    fun descExitFilter(descExitFilter: String): InteractiveSelectListBuilder = apply {
        config.descExitFilter = descExitFilter
    }

    /** Whether to show instructions at the bottom of the list */
    fun showInstructions(showInstructions: Boolean): InteractiveSelectListBuilder = apply {
        config.showInstructions = showInstructions
    }

    /** Set whether the list should be filterable */
    fun filterable(filterable: Boolean): InteractiveSelectListBuilder = apply {
        config.filterable = filterable
    }

    /**
     * Run the select list in single select mode.
     *
     * The [result][InputReceiver.Status.Finished.result] will be the selected item title, or `null`
     * if the user canceled the selection.
     */
    fun createSingleSelectInputAnimation(): InputReceiverAnimation<String?> {
        require(config.entries.isNotEmpty()) { "Select list must have at least one entry" }
        return SingleSelectInputAnimation(SelectInputAnimation(terminal, config, true))
    }

    /**
     * Run the select list in multi select mode.
     *
     * The [result][InputReceiver.Status.Finished.result] will be the selected item titles, or
     * `null` if the user canceled the selection.
     */
    fun createMultiSelectInputAnimation(): InputReceiverAnimation<List<String>?> {
        require(config.entries.isNotEmpty()) { "Select list must have at least one entry" }
        return SelectInputAnimation(terminal, config, false)
    }
}

private class SelectInputAnimation(
    override val terminal: Terminal,
    private val config: SelectConfig,
    private val singleSelect: Boolean,
) : InputReceiverAnimation<List<String>?> {
    private data class State(
        val items: List<SelectList.Entry>,
        val cursor: Int,
        val filtering: Boolean = false,
        val applyFilter: Boolean = false,
        val filter: String = "",
        val finished: Boolean = false,
        val result: List<String>? = null,
    ) {
        val filteredItems: List<SelectList.Entry>
            get() = if (!applyFilter || filter.isEmpty()) items else items.filter {
                filter.lowercase() in it.title.lowercase()
            }
    }

    private val state = MppAtomicRef(State(config.entries, config.startingCursorIndex))

    private val animation = terminal.animation<State> { s ->
        with(config) {
            SelectList(
                entries = if (onlyShowActiveDescription) {
                    s.filteredItems.mapIndexed { i, entry ->
                        entry.copy(
                            description = if (i == s.cursor) entry.description else null
                        )
                    }
                } else {
                    s.filteredItems
                },
                title = when {
                    s.filtering -> Text("${terminal.theme.style("select.cursor")("/")} ${s.filter}")
                    else -> title
                },
                cursorIndex = s.cursor,
                styleOnHover = singleSelect,
                cursorMarker = if (singleSelect || !s.filtering) cursorMarker else " ",
                selectedMarker = if (singleSelect) "" else selectedMarker,
                unselectedMarker = if (singleSelect) "" else unselectedMarker,
                selectedStyle = selectedStyle,
                unselectedTitleStyle = unselectedTitleStyle,
                unselectedMarkerStyle = unselectedMarkerStyle,
                captionBottom = when {
                    showInstructions -> {
                        Text(buildInstructions(singleSelect, s.filtering, s.filter.isNotEmpty()))
                    }

                    else -> null
                },
            )
        }
    }.apply { update(state.value) }

    override fun stop() = animation.stop()
    override fun clear() = animation.clear()

    override fun receiveEvent(event: InputEvent): Status<List<String>?> {
        if (event !is KeyboardEvent) return Status.Continue
        val (_, s) = state.update {
            with(config) {
                val filteredItems = filteredItems
                fun updateCursor(
                    newCursor: Int,
                    items: List<SelectList.Entry> = filteredItems,
                ): Int {
                    return newCursor.coerceAtMost(items.lastIndex).coerceAtLeast(0)
                }

                val key = event
                val entryIndex = when {
                    filteredItems.isEmpty() -> 0
                    applyFilter -> items.indexOf(filteredItems[cursor])
                    else -> cursor
                }
                val entry = items[entryIndex]
                when {
                    key.isCtrlC -> copy(finished = true)
                    key == keyPrev -> copy(cursor = updateCursor(cursor - 1))
                    key == keyNext -> copy(cursor = updateCursor(cursor + 1))
                    filterable && !filtering && key == keyFilter -> {
                        copy(filtering = true, applyFilter = true)
                    }

                    key == keyExitFilter -> {
                        copy(
                            filtering = false, applyFilter = false, filter = "", cursor = entryIndex
                        )
                    }

                    filtering && key == keySubmit && !singleSelect -> {
                        copy(filtering = false)
                    }

                    key == keySubmit -> {
                        if (singleSelect) copy(finished = true, result = listOf(entry.value ?: entry.title))
                        else copy(
                            finished = true,
                            result = items.filter { it.selected }.map { it.value ?: it.title })
                    }

                    filtering && !key.alt && !key.ctrl -> {
                        val s = copy(
                            filter = when {
                                key.key == "Backspace" -> filter.dropLast(1)
                                key.key.length == 1 -> filter + key.key
                                else -> filter // ignore modifier keys
                            }
                        )
                        s.copy(cursor = updateCursor(s.cursor, s.filteredItems))
                    }

                    !singleSelect && key == keyToggle -> {
                        if (entry.selected || items.count { it.selected } < limit) {
                            copy(
                                items = items.toMutableList().also {
                                    it[entryIndex] = entry.copy(selected = !entry.selected)
                                }
                            )
                        } else {
                            this@update // can't select more items
                        }
                    }

                    else -> this@update // unmapped key, no state change
                }
            }
        }
        animation.update(s)
        return when {
            s.finished -> {
                if (config.clearOnExit) animation.clear()
                else animation.stop()
                Status.Finished(s.result)
            }

            else -> Status.Continue
        }
    }

    private fun keyName(key: KeyboardEvent): String {
        var k = when (key.key) {
            "Enter" -> "enter"
            "Escape" -> "esc"
            "ArrowUp" -> "↑"
            "ArrowDown" -> "↓"
            "ArrowLeft" -> "←"
            "ArrowRight" -> "→"
            " " -> "space"
            else -> key.key
        }
        if (key.alt) k = "alt+$k"
        if (key.ctrl) k = "ctrl+$k"
        if (key.shift && key.key.length > 1) k = "shift+$k"
        return k
    }

    private fun buildInstructions(
        singleSelect: Boolean, filtering: Boolean, hasFilter: Boolean,
    ): String = with(config) {
        val s = TextStyle(brightWhite, bold = true)
        val parts = buildList {
            if (!singleSelect && !filtering) add(keyName(keyToggle) to descToggle)
            if (singleSelect || !filtering) {
                add(keyName(keyPrev) to descPrev)
                add(keyName(keyNext) to descNext)
            }
            if (filterable && !filtering) add(keyName(keyFilter) to descFilter)
            if (filtering || hasFilter) add(keyName(keyExitFilter) to descExitFilter)
            val submitDesc = when {
                singleSelect -> descSubmit
                filtering -> descApplyFilter
                else -> descConfirm
            }
            add(keyName(keySubmit) to submitDesc)
        }

        return dim(parts.joinToString(" • ") { "${s(it.first)} ${it.second}" })
    }
}

private class SingleSelectInputAnimation(
    private val animation: SelectInputAnimation,
) : InputReceiverAnimation<String?> {
    override val terminal: Terminal get() = animation.terminal
    override fun stop() = animation.stop()
    override fun clear() = animation.clear()
    override fun receiveEvent(event: InputEvent): Status<String?> {
        return when (val status = animation.receiveEvent(event)) {
            is Status.Finished -> Status.Finished(status.result?.firstOrNull())
            is Status.Continue -> Status.Continue
        }
    }
}
