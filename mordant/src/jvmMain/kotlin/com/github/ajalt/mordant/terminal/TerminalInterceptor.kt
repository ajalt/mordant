package com.github.ajalt.mordant.terminal

internal fun interface TerminalInterceptor {
    fun intercept(request: PrintRequest): PrintRequest
}
