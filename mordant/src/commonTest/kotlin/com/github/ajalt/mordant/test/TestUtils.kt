package com.github.ajalt.mordant.test

import com.github.ajalt.mordant.internal.CR_IMPLIES_LF
import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.terminal.TerminalRecorder

fun String.normalizeHyperlinks(): String {
    var i = 1
    val regex = Regex(";id=([^;]+);")
    val map = mutableMapOf<String, Int>()
    regex.findAll(this).forEach { map.getOrPut(it.value) { i++ } }
    return regex.replace(this) { ";id=${map[it.value]};" }
}

fun String.visibleCrLf(): String {
    return replace("\r", "␍").replace("\n", "␊").replace(CSI, "␛")
}

// This handles the difference in wasm movements and the other targets
fun TerminalRecorder.normalizedOutput(): String {
    return if (CR_IMPLIES_LF) output().replace("\r${CSI}1A", "\r") else output()
}
fun TerminalRecorder.latestOutput(): String {
    return normalizedOutput().substringAfter("${CSI}0J").substringAfter('\r')
}
