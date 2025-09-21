package com.github.ajalt.mordant.test

import com.github.ajalt.mordant.internal.CR_IMPLIES_LF
import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.terminal.TerminalRecorder
import io.kotest.matchers.shouldBe

fun String.normalizeHyperlinks(): String {
    var i = 1
    val regex = Regex(";id=([^;]+);")
    val map = mutableMapOf<String, Int>()
    regex.findAll(this).forEach { map.getOrPut(it.value) { i++ } }
    return regex.replace(this) { ";id=${map[it.value]};" }
}

fun String.visibleCrLf(keepBreaks: Boolean = false): String {
    return replace("\r", "␍").replace("\n", if (keepBreaks) "\n" else "␊").replace(CSI, "␛")
}

private val upMove = Regex("${Regex.escape(CSI)}\\d+A")

// This handles the difference in wasm movements and the other targets
fun TerminalRecorder.normalizedOutput(): String {
    return if (CR_IMPLIES_LF) output().replace("\r${CSI}1A", "\r") else output()
}

fun TerminalRecorder.latestOutput(): String {
    return normalizedOutput()
        // remove everything before the last cursor movement
        .let { it.split(upMove).lastOrNull() ?: it }.substringAfter("\r")
        .replace("${CSI}0J", "") // remove clear screen command
}

infix fun String.shouldMatchRender(expected: String) = shouldMatchRender(expected, true)

fun String.shouldMatchRender(
    expected: String, trimMargin: Boolean,
    printWithIndent: String = "",
) {
    try {
        val trimmed = if (trimMargin) expected.trimMargin("░") else expected
        this shouldBe trimmed.replace("░", "")
    } catch (e: Throwable) {
        println()
        if (printWithIndent.isEmpty()) {
            println(this)
        } else {
            println(prependIndent(printWithIndent))
        }
        throw e
    }
}
