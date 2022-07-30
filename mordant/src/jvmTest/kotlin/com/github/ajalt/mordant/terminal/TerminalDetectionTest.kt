package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.internal.getTerminalSize
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlin.test.Test

class TerminalDetectionTest {
    @Test
    fun testDetectWidth() {
        getTerminalSize(1000, true).shouldNotBeNull()
    }
}
