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
    var keyPrev: KeyboardEvent = KeyboardEvent("ArrowUp"),
    var keySubmit: KeyboardEvent = KeyboardEvent("Enter"),
    var keyToggle: KeyboardEvent = KeyboardEvent("x"),
    var keyFilter: KeyboardEvent = KeyboardEvent("/"),
    var keyExitFilter: KeyboardEvent = KeyboardEvent("Escape"),
    var instructions: Widget? = null,
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
    fun entries(entries: List<SelectList.Entry>): InteractiveSelectListBuilder = apply {
        config.entries = entries.toMutableList()
    }

    /** Set the list of items to select from */
    fun entries(vararg entries: String): InteractiveSelectListBuilder = apply {
        entries(entries.toList())
    }

    /** Set the list of items to select from */
    @JvmName("entriesString")
    fun entries(entries: List<String>): InteractiveSelectListBuilder = apply {
        config.entries = entries.mapTo(mutableListOf()) { SelectList.Entry(it) }
    }

    /** Add an item to the list of items to select from */
    fun addEntry(entry: SelectList.Entry): InteractiveSelectListBuilder = apply {
        config.entries += entry
    }

    /** Add an item to the list of items to select from */
    fun addEntry(entry: String): InteractiveSelectListBuilder = apply {
        config.entries += SelectList.Entry(entry)
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

    /** Set the key to move the cursor up */
    fun keyPrev(keyPrev: KeyboardEvent): InteractiveSelectListBuilder = apply {
        config.keyPrev = keyPrev
    }

    /** Set the key to submit the selection */
    fun keySubmit(keySubmit: KeyboardEvent): InteractiveSelectListBuilder = apply {
        config.keySubmit = keySubmit
    }

    /** Set the key to toggle the selection of an item */
    fun keyToggle(keyToggle: KeyboardEvent): InteractiveSelectListBuilder = apply {
        config.keyToggle = keyToggle
    }

    /** Set the key to filter the list */
    fun keyFilter(keyFilter: KeyboardEvent): InteractiveSelectListBuilder = apply {
        config.keyFilter = keyFilter
    }

    /** Set the key to stop filtering the list */
    fun keyExitFilter(keyExitFilter: KeyboardEvent): InteractiveSelectListBuilder = apply {
        config.keyExitFilter = keyExitFilter
    }

    /** Set the instructions to display at the bottom of the list */
    fun instructions(instructions: Widget): InteractiveSelectListBuilder = apply {
        config.instructions = instructions
    }

    /** Set the instructions to display at the bottom of the list */
    fun instructions(instructions: String): InteractiveSelectListBuilder = apply {
        config.instructions = Text(instructions)
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
    fun createSingleSelectInputAnimation(): InputReceiver<String?> {
        require(config.entries.isNotEmpty()) { "Select list must have at least one entry" }
        return SingleSelectInputAnimation(SelectInputAnimation(terminal, config, true))
    }

    /**
     * Run the select list in multi select mode.
     *
     * The [result][InputReceiver.Status.Finished.result] will be the selected item titles, or
     * `null` if the user canceled the selection.
     */
    fun createMultiSelectInputAnimation(): InputReceiver<List<String>?> {
        require(config.entries.isNotEmpty()) { "Select list must have at least one entry" }
        return SelectInputAnimation(terminal, config, false)
    }
}

private open class SelectInputAnimation(
    private val terminal: Terminal,
    private val config: SelectConfig,
    private val singleSelect: Boolean,
) : InputReceiver<List<String>?> {
    private data class State(
        val items: List<SelectList.Entry>,
        val cursor: Int,
        val filtering: Boolean,
        val filter: String,
        val finished: Boolean,
        val result: List<String>?,
    )

    private val state = MppAtomicRef(
        State(
            config.entries, config.startingCursorIndex, false, "", false, null
        )
    )

    private val animation = terminal.animation<State> { s ->
        with(config) {
            SelectList(
                entries = if (s.filter.isEmpty()) s.items else s.items.filter {
                    s.filter.lowercase() in it.title.lowercase()
                },
                title = when {
                    s.filtering -> Text("${terminal.theme.style("select.cursor")("/")} ${s.filter}")
                    else -> title
                },
                cursorIndex = s.cursor,
                styleOnHover = singleSelect,
                selectedMarker = selectedMarker,
                cursorMarker = cursorMarker,
                unselectedMarker = unselectedMarker,
                selectedStyle = selectedStyle,
                unselectedTitleStyle = unselectedTitleStyle,
                unselectedMarkerStyle = unselectedMarkerStyle,
                captionBottom = instructions
                    ?: Text(buildInstructions(singleSelect, s.filtering, s.filter.isNotEmpty())),
            )
        }
    }.apply { update(state.value) }


    override fun onInput(event: KeyboardEvent): Status<List<String>?> {
        val (_, s) = state.update {
            with(config) {
                fun updateCursor(newCursor: Int): State = copy(
                    cursor = newCursor.coerceIn(0, entries.lastIndex),
                    items = if (onlyShowActiveDescription) {
                        items.mapIndexed { i, entry ->
                            entry.copy(
                                description = if (i == cursor) entries[i].description else null
                            )
                        }
                    } else items
                )

                val key = event
                val entry = items[cursor]
                when {
                    key.isCtrlC -> copy(finished = true)
                    key == keyPrev -> updateCursor(cursor - 1)
                    key == keyNext -> updateCursor(cursor + 1)
                    filterable && !filtering && key == keyFilter -> {
                        copy(filtering = true)
                    }

                    filtering && key == keyExitFilter -> {
                        copy(items = entries, filtering = false)
                    }

                    filtering && !singleSelect && key == keySubmit -> {
                        copy(filtering = false)
                    }

                    filtering && !key.alt && !key.ctrl -> {
                        copy(
                            filter = when {
                                key.key == "Backspace" -> filter.dropLast(1)
                                key.key.length == 1 -> filter + key.key
                                else -> filter // ignore modifier keys
                            }
                        )
                    }

                    !singleSelect && key == keyToggle -> {
                        if (entry.selected || items.count { it.selected } < limit) {
                            copy(
                                items = items.toMutableList().also {
                                    it[cursor] = entry.copy(selected = !entry.selected)
                                }
                            )
                        } else {
                            this@update // can't select more items
                        }
                    }

                    key == keySubmit -> {
                        if (singleSelect) copy(finished = true, result = listOf(entry.title))
                        else copy(
                            finished = true,
                            result = items.filter { it.selected }.map { it.title })
                    }

                    else -> this@update // unmapped key, no state change
                }
            }
        }
        return if (s.finished) Status.Finished(s.result)
        else Status.Continue
    }

    private fun keyName(key: KeyboardEvent): String {
        var k = when (key.key) {
            "Enter" -> "enter"
            "Escape" -> "esc"
            "ArrowUp" -> "↑"
            "ArrowDown" -> "↓"
            "ArrowLeft" -> "←"
            "ArrowRight" -> "→"
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
            add(keyName(keyPrev) to "up")
            add(keyName(keyNext) to "down")
            if (filtering) {
                if (!singleSelect) add(keyName(keySubmit) to "apply filter")
            } else if (filterable) {
                add(keyName(keyFilter) to "filter")
            }
            if (filtering || hasFilter) {
                add(keyName(keyExitFilter) to "clear filter")
            }
            if (singleSelect) {
                add(keyName(keySubmit) to "select")
            } else {
                add(keyName(keyToggle) to "toggle")
                add(keyName(keySubmit) to "confirm")
            }
        }

        return dim(parts.joinToString(" • ") { "${s(it.first)} ${it.second}" })
    }
}

private class SingleSelectInputAnimation(
    private val animation: SelectInputAnimation,
) : InputReceiver<String?> {
    override fun onInput(event: KeyboardEvent): Status<String?> {
        return when (val status = animation.onInput(event)) {
            is Status.Finished -> Status.Finished(status.result?.firstOrNull())
            is Status.Continue -> Status.Continue
        }
    }
}
