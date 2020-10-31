package com.github.ajalt.mordant.internal

import java.util.concurrent.atomic.AtomicInteger

private val nextHyperlinkId = AtomicInteger(1)

// This is a var so we can override it in tests
internal var generateHyperlinkId: () -> String = {
    nextHyperlinkId.getAndIncrement().toString()
}
