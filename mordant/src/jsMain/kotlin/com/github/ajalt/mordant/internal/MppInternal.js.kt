package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.terminal.terminalinterface.TerminalInterfaceJsCommon
import com.github.ajalt.mordant.terminal.terminalinterface.TerminalInterfaceJsNode

private external val process: dynamic
private external val console: dynamic
private external val Symbol: dynamic
private external val Buffer: dynamic

internal actual fun browserPrintln(message: String) {
    // No way to avoid the newline on browsers
    console.error(message)
}

internal actual fun makeNodeTerminalInterface(): TerminalInterfaceJsCommon? {
    return try {
        TerminalInterfaceJsNode(nodeRequire("fs"))
    } catch (e: Exception) {
        null
    }
}

internal actual fun codepointSequence(string: String): Sequence<Int> {
    val it = string.asDynamic()[Symbol.iterator]()
    return generateSequence {
        it.next()["value"]?.codePointAt(0) as? Int
    }
}

internal actual val CR_IMPLIES_LF: Boolean = false
