package com.github.ajalt.mordant.samples

import com.github.ajalt.mordant.input.interactiveSelectList
import com.github.ajalt.mordant.rendering.BorderType.Companion.SQUARE_DOUBLE_SECTION_SEPARATOR
import com.github.ajalt.mordant.rendering.TextAlign.LEFT
import com.github.ajalt.mordant.rendering.TextAlign.RIGHT
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.table.Borders.*
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal


fun main() {
    val terminal = Terminal()
    val result = terminal.interactiveSelectList(
        listOf("United States", "Canada", "Mexico"),
        title = "Select a country",
    )
    terminal.info("Selected: $result")
}
