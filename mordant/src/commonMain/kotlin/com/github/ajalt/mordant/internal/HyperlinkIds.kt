package com.github.ajalt.mordant.internal

import kotlin.native.concurrent.ThreadLocal

private val nextHyperlinkId = AtomicInt(1)

// This is a var so we can override it in tests
@ThreadLocal
internal var generateHyperlinkId: () -> String = {
    nextHyperlinkId.getAndIncrement().toString()
}
