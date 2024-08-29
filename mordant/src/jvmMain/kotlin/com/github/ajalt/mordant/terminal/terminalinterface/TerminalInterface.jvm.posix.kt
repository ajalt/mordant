package com.github.ajalt.mordant.terminal.terminalinterface

import kotlin.time.TimeMark

/**
 * A base class for terminal interfaces for JVM POSIX systems that uses `System.in` for input.
 */
abstract class TerminalInterfaceJvmPosix : TerminalInterfacePosix() {
    override fun readRawByte(timeout: TimeMark): Int {
        do {
            val c = System.`in`.read()
            if (c >= 0) return c
        } while (timeout.hasNotPassedNow())
        throw RuntimeException("Timeout reading from stdin (timeout=$timeout)")
    }
}
