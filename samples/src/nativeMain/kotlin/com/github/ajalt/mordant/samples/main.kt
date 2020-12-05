package com.github.ajalt.mordant.samples

import kotlinx.cinterop.*
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen

fun main(args: Array<String>) {
    printMarkdown(args)
}

actual fun readText(filePath: String): String {
    val file = checkNotNull(fopen(filePath, "r")) {
        "No file contents for $filePath"
    }
    val bufferLength = 64 * 1024
    return buildString {
        try {
            memScoped {
                val buffer = allocArray<ByteVar>(bufferLength)
                var line: String?
                do {
                    line = fgets(buffer, bufferLength, file)
                            ?.toKStringFromUtf8()
                            ?.also(::append)
                } while (line != null)
            }
        } finally {
            fclose(file)
        }
    }
}
