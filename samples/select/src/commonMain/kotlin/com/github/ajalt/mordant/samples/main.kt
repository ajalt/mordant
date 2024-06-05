package com.github.ajalt.mordant.samples

import com.github.ajalt.mordant.input.interactiveMultiSelectList
import com.github.ajalt.mordant.input.interactiveSelectList
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.SelectList.Entry


fun main() {
    val terminal = Terminal()
    val theme = terminal.theme
    val size = terminal.interactiveSelectList(
        listOf("Small", "Medium", "Large", "X-Large"),
        title = "Select a Pizza Size",
    )
    if (size == null) {
        terminal.danger("Aborted pizza order")
        return
    }
    val toppings = terminal.interactiveMultiSelectList(
        listOf(
            Entry("Pepperoni", selected = true, description = "Spicy"),
            Entry("Sausage", selected = true, description = "Spicy"),
            Entry("Mushrooms", description = "Fresh, not canned"),
            Entry("Olives", description = "Black olives"),
            Entry("Pineapple", description = "Fresh, not canned"),
            Entry("Anchovies", description = "Please don't"),
        ),
        title = "Select Toppings",
        limit = 4,
        onlyShowActiveDescription = true,
    )
    if (toppings == null) {
        terminal.danger("Aborted pizza order")
        return
    }
    val toppingString = if (toppings.isEmpty()) "no toppings" else toppings.joinToString()
    terminal.success("You ordered a ${theme.info(size)} pizza with ${theme.info(toppingString)}")
}
