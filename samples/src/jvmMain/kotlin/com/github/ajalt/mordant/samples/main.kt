package com.github.ajalt.mordant.samples

import java.io.File

fun main(args: Array<String>) {
    printMarkdown(args)
}

actual fun readText(filePath: String): String {
    return checkNotNull(File(filePath).readText()) {
        "No file contents for $filePath"
    }
}
