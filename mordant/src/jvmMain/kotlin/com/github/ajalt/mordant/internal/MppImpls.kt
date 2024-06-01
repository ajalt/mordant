package com.github.ajalt.mordant.internal

import com.github.ajalt.mordant.input.KeyboardEvent
import kotlin.time.Duration


internal interface MppImpls {
    fun stdoutInteractive(): Boolean
    fun stdinInteractive(): Boolean
    fun stderrInteractive(): Boolean
    fun getTerminalSize(): Size?
    fun fastIsTty(): Boolean = true
    fun readKey(timeout: Duration): KeyboardEvent?
    fun enterRawMode(): AutoCloseable
}


/** A non-JNA implementation for unimplemented OSes like FreeBSD */
internal class FallbackMppImpls : MppImpls {
    override fun stdoutInteractive(): Boolean = System.console() != null
    override fun stdinInteractive(): Boolean = System.console() != null
    override fun stderrInteractive(): Boolean = System.console() != null
    override fun getTerminalSize(): Size? = null
    override fun fastIsTty(): Boolean = false
    override fun readKey(timeout: Duration): KeyboardEvent? = null
    override fun enterRawMode(): AutoCloseable = AutoCloseable { }
}
