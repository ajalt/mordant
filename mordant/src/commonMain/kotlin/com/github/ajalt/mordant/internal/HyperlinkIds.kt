package com.github.ajalt.mordant.internal

private val nextHyperlinkId = AtomicInt(1)

// This is a var so we can override it in tests
internal var generateHyperlinkId: () -> String = {
    nextHyperlinkId.getAndIncrement().toString()
}
