package com.github.ajalt.mordant.terminal

@ExperimentalTerminalApi
internal fun interface TerminalInterceptor {
    fun intercept(request: PrintRequest): PrintRequest
}
