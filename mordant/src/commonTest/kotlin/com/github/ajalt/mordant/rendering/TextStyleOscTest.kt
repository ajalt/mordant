package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.terminal.TextStyles.Companion.hyperlink
import com.github.ajalt.mordant.internal.OSC
import com.github.ajalt.mordant.internal.ST
import com.github.ajalt.mordant.internal.generateHyperlinkId
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TextStyleOscTest {
    init {
        generateHyperlinkId = { "x" }
    }

    @Test
    fun `single hyperlink`() = doTest(
            hyperlink("foo.com")("bar"),
            "<;id=x;foo.com>bar<;;>"
    )

    @Test
    fun `nested hyperlink`() {
        var i = 1
        generateHyperlinkId = { (i++).toString() }
        doTest(
                hyperlink("foo")("bar${hyperlink("baz")("qux")}qor"),
                "<;id=1;foo>bar<;id=2;baz>qux<;id=1;foo>qor<;;>"
        )
    }

    private fun doTest(actual: String, expected: String) {
        try {
            actual.replace("${OSC}8", "<").replace(ST, ">") shouldBe expected
        } catch (e: Throwable) {
            println(actual)
            throw e
        }
    }
}
