package com.github.ajalt.mordant.internal


internal interface MppImpls {
    fun stdoutInteractive(): Boolean
    fun stdinInteractive(): Boolean
    fun stderrInteractive(): Boolean
    fun getTerminalSize(): Pair<Int, Int>?
    fun fastIsTty(): Boolean = true
}


/** A non-JNA implementation for unimplemented OSes like FreeBSD */
internal class FallbackMppImpls : MppImpls {
    override fun stdoutInteractive(): Boolean = System.console() != null
    override fun stdinInteractive(): Boolean = System.console() != null
    override fun stderrInteractive(): Boolean = System.console() != null
    override fun getTerminalSize(): Pair<Int, Int>? = null
    override fun fastIsTty(): Boolean = false
}
