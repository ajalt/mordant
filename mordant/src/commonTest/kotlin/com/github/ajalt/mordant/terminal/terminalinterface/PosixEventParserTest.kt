package com.github.ajalt.mordant.terminal.terminalinterface

import com.github.ajalt.mordant.input.KeyboardEvent
import com.github.ajalt.mordant.input.MouseEvent
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class PosixEventParserTest {
    private val esc = Char(0x1b)

    @[Test JsName("keyboard_event")]
    fun `keyboard event`() {
        parse("$esc[A") shouldBe KeyboardEvent("ArrowUp")
    }

    @[Test JsName("legacy_mouse_events")]
    fun `legacy mouse events`() = forAll(
        // cb is the raw byte, so button codes are offset by 32
        row("left press", 32, MouseEvent(0, 0, left = true)),
        row("middle press", 33, MouseEvent(0, 0, middle = true)),
        row("right press", 34, MouseEvent(0, 0, right = true)),
        row("release", 35, MouseEvent(0, 0)),
        row("left drag", 64, MouseEvent(0, 0, left = true)),
        row("wheel up", 96, MouseEvent(0, 0, wheelUp = true)),
        row("wheel down", 97, MouseEvent(0, 0, wheelDown = true)),
        row("ctrl+left press", 48, MouseEvent(0, 0, left = true, ctrl = true)),
    ) { _, cb, expected ->
        parse("$esc[M${cb.toChar()}!!") shouldBe expected
    }

    @[Test JsName("legacy_mouse_event_coordinates")]
    fun `legacy mouse event coordinates`() {
        parse("$esc[M 0B") shouldBe MouseEvent(15, 33, left = true)
    }

    @[Test JsName("sgr_mouse_events")]
    fun `sgr mouse events`() = forAll(
        row("left press", "0;1;1M", MouseEvent(0, 0, left = true)),
        row("left release", "0;1;1m", MouseEvent(0, 0)),
        row("middle press", "1;1;1M", MouseEvent(0, 0, middle = true)),
        row("right press", "2;1;1M", MouseEvent(0, 0, right = true)),
        row("left drag", "32;3;2M", MouseEvent(2, 1, left = true)),
        row("motion without button", "35;1;1M", MouseEvent(0, 0)),
        row("wheel up", "64;1;1M", MouseEvent(0, 0, wheelUp = true)),
        row("wheel down", "65;1;1M", MouseEvent(0, 0, wheelDown = true)),
        row("wheel left", "66;1;1M", MouseEvent(0, 0, wheelLeft = true)),
        row("wheel right", "67;1;1M", MouseEvent(0, 0, wheelRight = true)),
        row("mouse4 press", "128;1;1M", MouseEvent(0, 0, mouse4 = true)),
        row("mouse5 press", "129;1;1M", MouseEvent(0, 0, mouse5 = true)),
        row("shift+left press", "4;1;1M", MouseEvent(0, 0, left = true, shift = true)),
        row("alt+left press", "8;1;1M", MouseEvent(0, 0, left = true, alt = true)),
        row("ctrl+wheel down", "81;1;1M", MouseEvent(0, 0, wheelDown = true, ctrl = true)),
        row("large coordinates", "0;500;300M", MouseEvent(499, 299, left = true)),
    ) { _, sequence, expected ->
        parse("$esc[<$sequence") shouldBe expected
    }

    @[Test JsName("malformed_sgr_mouse_events")]
    fun `malformed sgr mouse events`() = forAll(
        row("missing fields", "0;1M"),
        row("extra fields", "0;1;1;1M"),
        row("empty fields", ";;M"),
        row("invalid character", "0;1;1x"),
    ) { _, sequence ->
        parse("$esc[<$sequence") shouldBe null
    }

    @[Test JsName("sequential_mixed_mouse_events")]
    fun `sequential mixed mouse events`() {
        val parser = parserFor("$esc[<0;2;2M$esc[M !!$esc[<0;2;2m$esc[A")
        readEvent(parser) shouldBe MouseEvent(1, 1, left = true)
        readEvent(parser) shouldBe MouseEvent(0, 0, left = true)
        readEvent(parser) shouldBe MouseEvent(1, 1)
        readEvent(parser) shouldBe KeyboardEvent("ArrowUp")
    }

    private fun parse(input: String): Any? = readEvent(parserFor(input))

    private fun parserFor(input: String): PosixEventParser {
        val bytes = input.encodeToByteArray().iterator()
        return PosixEventParser {
            if (bytes.hasNext()) bytes.next().toInt() and 0xff else null
        }
    }

    private fun readEvent(parser: PosixEventParser): Any? {
        return parser.readInputEvent(TimeSource.Monotonic.markNow() + 1.seconds)
    }
}
