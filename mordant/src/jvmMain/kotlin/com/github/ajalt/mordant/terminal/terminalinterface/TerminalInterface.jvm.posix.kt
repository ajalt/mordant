package com.github.ajalt.mordant.terminal.terminalinterface

/**
 * A base class for terminal interfaces for JVM POSIX systems that uses `System.in` for input.
 */
abstract class TerminalInterfaceJvmPosix : TerminalInterfacePosix() {
    override fun readRawByte(): Int? {
        return System.`in`.read().takeUnless { it <= 0 }
    }
}
