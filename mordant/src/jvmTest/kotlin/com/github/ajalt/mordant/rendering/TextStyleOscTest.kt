package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.rendering.internal.OSC
import com.github.ajalt.mordant.rendering.internal.ST
import io.kotest.matchers.shouldBe
import org.junit.Test

class TextStyleOscTest {
    @Test
    fun `single hyperlink`() = doTest(
            TextStyle(hyperlink = "foo.com")("bar"),
            "<;;foo.com>bar<;;>"
    )

    @Test
    fun `nested hyperlink`() = doTest(
            TextStyle(hyperlink = "foo")("bar${TextStyle(hyperlink = "baz")("qux")}qor"),
            "<;;foo>bar<;;baz>qux<;;foo>qor<;;>"
    )

    private fun doTest(actual: String, expected: String) {
        try {
            actual.replace("${OSC}8", "<").replace(ST, ">") shouldBe expected
        } catch (e: Throwable) {
            println(actual)
            throw e
        }
    }
}
