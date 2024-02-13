package com.github.ajalt.mordant.test

import com.github.ajalt.mordant.internal.CSI
import com.github.ajalt.mordant.terminal.TerminalRecorder

fun String.normalizeHyperlinks(): String {
    var i = 1
    val regex = Regex(";id=([^;]+);")
    val map = mutableMapOf<String, Int>()
    regex.findAll(this).forEach { map.getOrPut(it.value) { i++ } }
    return regex.replace(this) { ";id=${map[it.value]};" }
}

fun String.replaceCrLf(): String {
    return replace("\r", "␍").replace("\n", "␊").replace(CSI, "␛")
}

fun TerminalRecorder.normalizedOutput(): String {
    return output().substringAfter("${CSI}0J").substringAfter('\r')
}
