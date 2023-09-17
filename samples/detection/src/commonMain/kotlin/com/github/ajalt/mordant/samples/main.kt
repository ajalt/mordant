package com.github.ajalt.mordant.samples

import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal

fun main() {
    val terminal = Terminal()
    terminal.println(Theme.Default.info(terminal.info.toString()))
    terminal.println(
        "Theme colors: " +
                "${Theme.Default.success("success")}, " +
                "${Theme.Default.danger("danger")}, " +
                "${Theme.Default.warning("warning")}, " +
                "${Theme.Default.info("info")}, " +
                "${Theme.Default.muted("muted")}"
    )
}
