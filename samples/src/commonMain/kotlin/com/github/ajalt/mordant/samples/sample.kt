package com.github.ajalt.mordant.samples

import com.github.ajalt.mordant.terminal.Terminal

fun printMarkdown(args: Array<String>) {
    val path = args.singleOrNull() ?: error("must specify a markdown file")
    Terminal().printMarkdown(readText(path))
}

expect fun readText(filePath: String): String
