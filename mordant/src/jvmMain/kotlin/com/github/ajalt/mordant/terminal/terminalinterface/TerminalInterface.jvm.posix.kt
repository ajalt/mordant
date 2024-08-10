package com.github.ajalt.mordant.terminal.terminalinterface

import kotlin.time.ComparableTimeMark
import kotlin.time.Duration

// TODO: docs
abstract class TerminalInterfaceJvmPosix : TerminalInterfacePosix() {
    override fun readRawByte(t0: ComparableTimeMark, timeout: Duration): Char {
        do {
            val c = System.`in`.read()
            if (c >= 0) return c.toChar()
        } while (t0.elapsedNow() < timeout)
        throw RuntimeException("Timeout reading from stdin (timeout=$timeout)")
    }
}
