package com.github.ajalt.mordant.terminal.terminalinterface

import com.github.ajalt.mordant.input.MouseEvent
import io.kotest.matchers.shouldBe
import kotlin.test.assertIs
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class PosixEventParserTest {
    @Test
    fun `parses SGR wheel up`() {
        val event = readEvent("\u001b[<64;11;7M") as MouseEvent
        event.wheelUp shouldBe true
        event.wheelDown shouldBe false
        event.x shouldBe 10
        event.y shouldBe 6
    }

    @Test
    fun `parses SGR left press and release`() {
        val press = readEvent("\u001b[<0;5;3M") as MouseEvent
        press.left shouldBe true
        press.middle shouldBe false
        press.right shouldBe false

        val release = readEvent("\u001b[<0;5;3m") as MouseEvent
        release.left shouldBe false
        release.middle shouldBe false
        release.right shouldBe false
    }

    @Test
    fun `parses SGR right and middle buttons`() {
        val middle = readEvent("\u001b[<1;8;4M") as MouseEvent
        middle.middle shouldBe true
        middle.right shouldBe false

        val right = readEvent("\u001b[<2;8;4M") as MouseEvent
        right.middle shouldBe false
        right.right shouldBe true
    }

    @Test
    fun `parses SGR modifiers`() {
        val event = readEvent("\u001b[<20;3;2M") as MouseEvent
        event.shift shouldBe true
        event.alt shouldBe false
        event.ctrl shouldBe true
    }

    @Test
    fun `legacy X10 wheel with motion bit still maps as wheel`() {
        val event = readEvent(x10(cb = 96, cx = 33, cy = 33)) as MouseEvent
        event.wheelUp shouldBe true
        event.wheelDown shouldBe false
    }

    @Test
    fun `legacy X10 keeps original middle and right mapping`() {
        val right = readEvent(x10(cb = 1 + 32, cx = 40, cy = 40)) as MouseEvent
        right.right shouldBe true
        right.middle shouldBe false

        val middle = readEvent(x10(cb = 2 + 32, cx = 40, cy = 40)) as MouseEvent
        middle.middle shouldBe true
        middle.right shouldBe false
    }

    @Test
    fun `parser handles SGR and then X10 sequence`() {
        val bytes = ("\u001b[<0;5;3M" + x10(cb = 0 + 32, cx = 35, cy = 36)).encodeToByteArray().map { it.toInt() and 0xff }
        var i = 0
        val parser = PosixEventParser { if (i < bytes.size) bytes[i++] else null }

        val first = assertIs<MouseEvent>(parser.readInputEvent(TimeSource.Monotonic.markNow() + 1.seconds))
        first.left shouldBe true
        first.x shouldBe 4
        first.y shouldBe 2

        val second = assertIs<MouseEvent>(parser.readInputEvent(TimeSource.Monotonic.markNow() + 1.seconds))
        second.left shouldBe true
        second.x shouldBe 2
        second.y shouldBe 3
    }

    private fun readEvent(input: String): Any {
        val bytes = input.encodeToByteArray().map { it.toInt() and 0xff }
        var i = 0
        val parser = PosixEventParser {
            if (i < bytes.size) bytes[i++] else null
        }
        return parser.readInputEvent(TimeSource.Monotonic.markNow() + 1.seconds)!!
    }

    private fun x10(cb: Int, cx: Int, cy: Int): String {
        return buildString {
            append("\u001b[M")
            append(cb.toChar())
            append(cx.toChar())
            append(cy.toChar())
        }
    }
}
