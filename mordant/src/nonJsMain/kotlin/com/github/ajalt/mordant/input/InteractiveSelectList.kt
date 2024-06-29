package com.github.ajalt.mordant.input

import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.SelectList
import kotlin.jvm.JvmName

/**
 * Display a list of items and allow the user to select one with the arrow keys and enter.
 *
 * @return the selected item title, or null if the user canceled the selection
 */
inline fun Terminal.interactiveSelectList(
    block: InteractiveSelectListBuilder.() -> Unit,
): String? {
    return InteractiveSelectListBuilder(this)
        .apply(block)
        .createSingleSelectInputAnimation()
        .receiveEvents()
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
    entries: Iterable<String>,
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
    entries: Iterable<SelectList.Entry>,
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
    return InteractiveSelectListBuilder(this)
        .apply(block)
        .createMultiSelectInputAnimation()
        .receiveEvents()
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
    entries: Iterable<SelectList.Entry>,
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
    entries: Iterable<String>,
    title: String = "",
): List<String>? {
    return interactiveMultiSelectList {
        entries(entries)
        title(title)
    }
}
