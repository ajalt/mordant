package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ThemeTest {
    private val theme = Theme(Theme.PlainAscii) {
        styles["s"] = TextColors.blue
    }

    @Test
    fun style() {
        theme.style("s") shouldBe TextColors.blue
        theme.style("foo") shouldBe DEFAULT_STYLE
        theme.style("foo", TextColors.blue) shouldBe TextColors.blue
    }

    @Test
    fun styleOrNull() {
        theme.styleOrNull("s") shouldBe TextColors.blue
        theme.styleOrNull("foo") shouldBe null
    }

    @Test
    fun flag() {
        theme.flag("markdown.code.block.border") shouldBe true
        theme.flag("foo") shouldBe false
        theme.flag("foo", true) shouldBe true
    }

    @Test
    fun flagOrNull() {
        theme.flagOrNull("markdown.code.block.border") shouldBe true
        theme.flagOrNull("foo") shouldBe null
    }

    @Test
    fun string() {
        theme.string("list.number.separator") shouldBe "."
        theme.string("foo") shouldBe ""
        theme.string("foo", "bar") shouldBe "bar"
    }

    @Test
    fun stringOrNull() {
        theme.stringOrNull("list.number.separator") shouldBe "."
        theme.stringOrNull("foo") shouldBe null
    }

    @Test
    fun dimension() {
        theme.dimension("hr.title.padding") shouldBe 1
        theme.dimension("foo") shouldBe 0
        theme.dimension("foo", -1) shouldBe -1
    }

    @Test
    fun dimensionOrNull() {
        theme.dimensionOrNull("hr.title.padding") shouldBe 1
        theme.dimensionOrNull("foo") shouldBe null
    }

    @Test
    fun plus() {
        val l = Theme {
            dimensions["foo"] = 11
            dimensions["bar"] = 22
        }
        val r = Theme {
            dimensions["bar"] = 23
            dimensions["baz"] = 33
        }
        val t = l + r
        t.dimension("foo") shouldBe 11
        t.dimension("bar") shouldBe 23
        t.dimension("baz") shouldBe 33
    }
}
