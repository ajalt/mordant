package com.github.ajalt.mordant.test

fun String.normalizeHyperlinks(): String {
    var i = 1
    val regex = Regex(";id=([^;]+);")
    val map = mutableMapOf<String, Int>()
    regex.findAll(this).forEach { map.getOrPut(it.value) { i++ } }
    return regex.replace(this) { ";id=${map[it.value]};" }
}
