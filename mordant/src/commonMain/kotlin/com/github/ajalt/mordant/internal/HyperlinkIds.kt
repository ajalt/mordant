package com.github.ajalt.mordant.internal


private val nextHyperlinkId = MppAtomicInt(1)

internal fun generateHyperlinkId(): String {
    return nextHyperlinkId.getAndIncrement().toString()
}
