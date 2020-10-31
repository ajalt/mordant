package com.github.ajalt.mordant.internal

internal object AnsiCodes {
    val fg16Range = 30..37
    val fg16BrightRange = 90..97
    const val fgColorSelector = 38
    const val fgColorReset = 39

    const val fgBgOffset = 10

    val bg16Range = 40..47
    val bg16BrightRange = 100..107
    const val bgColorSelector = 48
    const val bgColorReset = 49

    const val selector256 = 5
    const val selectorRgb = 2

    const val underlineColorSelector = 58

    const val reset = 0
    const val boldOpen = 1
    const val boldAndDimClose = 22
    const val dimOpen = 2
    const val italicOpen = 3
    const val italicClose = 23
    const val underlineOpen = 4
    const val underlineClose = 24
    const val inverseOpen = 7
    const val inverseClose = 27
    const val strikethroughOpen = 9
    const val strikethroughClose = 29
}

internal const val ESC = "\u001B"

/** Control Sequence Introducer */
internal const val CSI = "$ESC["

/** Operating System Command */
internal const val OSC = "$ESC]"

/** String Terminator */
internal const val ST = "$ESC\\"
