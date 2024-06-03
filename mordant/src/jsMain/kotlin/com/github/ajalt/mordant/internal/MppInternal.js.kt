package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.internal.syscalls.SyscallHandlerJsCommon
import com.github.ajalt.mordant.internal.syscalls.SyscallHandlerJsNode
import com.github.ajalt.mordant.internal.syscalls.SyscallHandlerNode
import com.github.ajalt.mordant.terminal.PrintTerminalCursor
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalCursor

private external val process: dynamic
private external val console: dynamic
private external val Symbol: dynamic
private external val Buffer: dynamic

internal actual fun browserPrintln(message: String) {
    // No way to avoid the newline on browsers
    console.error(message)
}

internal actual fun makeNodeSyscallHandler(): SyscallHandlerJsCommon? {
    return try {
        SyscallHandlerJsNode(nodeRequire("fs"))
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
