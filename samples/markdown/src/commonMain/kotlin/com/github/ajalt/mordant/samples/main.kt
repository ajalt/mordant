package com.github.ajalt.mordant.samples

import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.terminal.Terminal
import java.io.File

fun main(args: Array<String>) {
    val terminal = Terminal()
    val path = args.singleOrNull() ?: error("must specify a markdown file")
    val markdown = File(path).readText()
    val widget = Markdown(markdown)
    terminal.println(widget)
}
