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
    val toppings = terminal.interactiveMultiSelectList {
        entries(
            Entry("Pepperoni", selected = true),
            Entry("Sausage", selected = true),
            Entry("Mushrooms"),
            Entry("Olives"),
            Entry("Pineapple"),
            Entry("Anchovies"),
        )
        title("Select Toppings")
        limit(4)
    }

    if (toppings == null) {
        terminal.danger("Aborted pizza order")
        return
    }
    val toppingString = if (toppings.isEmpty()) "no toppings" else toppings.joinToString()
    terminal.success("You ordered a ${theme.info(size)} pizza with ${theme.info(toppingString)}")
}
