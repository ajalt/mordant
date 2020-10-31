package com.github.ajalt.mordant.samples

import com.github.ajalt.mordant.terminal.Terminal
import java.io.File

fun main(args: Array<String>) {
    val path = args.singleOrNull() ?: error("must specify a markdown file")
    Terminal().printMarkdown(File(path).readText())
}
