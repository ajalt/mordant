package com.github.ajalt.mordant.internal

import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
private val nextHyperlinkId = AtomicInt(1)

internal fun generateHyperlinkId(): String {
    return nextHyperlinkId.getAndIncrement().toString()
}
