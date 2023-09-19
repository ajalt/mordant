package com.github.ajalt.mordant.samples

import com.github.ajalt.mordant.rendering.Whitespace.NORMAL
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Panel
import com.github.ajalt.mordant.widgets.Text
import com.github.ajalt.mordant.widgets.withPadding

fun main() {
    val terminal = Terminal()
    val theme = terminal.theme
    terminal.println(
        Panel(
            Text(terminal.info.toString(), whitespace = NORMAL).withPadding(1),
            Text(theme.info("Detected Terminal Info"))
        )
    )
    terminal.println(
        Panel(
            Text(
                "${theme.success("success")}, " +
                        "${theme.danger("danger")}, " +
                        "${theme.warning("warning")}, " +
                        "${theme.info("info")}, " +
                        theme.muted("muted")
            ).withPadding(1),
            Text(theme.info("Theme colors"))
        )
    )
}
