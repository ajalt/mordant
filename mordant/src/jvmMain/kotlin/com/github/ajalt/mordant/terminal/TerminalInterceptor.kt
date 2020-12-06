package com.github.ajalt.mordant.terminal

fun interface TerminalInterceptor {
    fun intercept(request: PrintRequest): PrintRequest
}
