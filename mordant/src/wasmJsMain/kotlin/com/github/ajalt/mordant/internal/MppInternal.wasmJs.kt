package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.`interface`.TerminalInterfaceJsCommon
import com.github.ajalt.mordant.terminal.`interface`.TerminalInterfaceWasm

internal actual fun browserPrintln(message: String): Unit = js("console.error(message)")

internal actual fun makeNodeTerminalInterface(): TerminalInterfaceJsCommon? {
    return if (runningOnNode()) TerminalInterfaceWasm() else null
}

private external interface CodePointString {
    fun codePointAt(index: Int): Int
}

private external interface StringIteration {
    val value: CodePointString?
}

private external interface StringIterator {
    fun next(): StringIteration
}

private fun stringIterator(@Suppress("UNUSED_PARAMETER") s: String): StringIterator =
    js("s[Symbol.iterator]()")

internal actual fun codepointSequence(string: String): Sequence<Int> {
    val it = stringIterator(string)
    return generateSequence { it.next().value?.codePointAt(0) }
}

// See jsMain for the details of node detection
private fun runningOnNode(): Boolean =
    js("Object.prototype.toString.call(typeof process !== 'undefined' ? process : 0) === '[object process]'")


// For some reason, \r seems to be treated as \r\n on wasm
internal actual val CR_IMPLIES_LF: Boolean = true
