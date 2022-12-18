package com.github.ajalt.mordant.internal


internal interface JnaMppImpls {
    fun stdoutInteractive(): Boolean
    fun stdinInteractive(): Boolean
    fun stderrInteractive(): Boolean
    fun getTerminalSize(): Pair<Int, Int>?
    val isWindows: Boolean
}


