package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.internal.OSC
import com.github.ajalt.mordant.internal.ST
import com.github.ajalt.mordant.rendering.TextStyles.Companion.hyperlink
import com.github.ajalt.mordant.test.normalizeHyperlinks
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TextStyleOscTest {
    @Test
    fun `single hyperlink`() = doTest(
        hyperlink("foo.com")("bar"),
        "<;id=1;foo.com>bar<;;>"
    )

    @Test
    fun `nested hyperlink`() = doTest(
        hyperlink("foo")("bar${hyperlink("baz")("qux")}qor"),
        "<;id=1;foo>bar<;id=2;baz>qux<;id=1;foo>qor<;;>"
    )

    private fun doTest(actual: String, expected: String) {
        val normalized = actual.replace("${OSC}8", "<").replace(ST, ">").normalizeHyperlinks()
        try {
            normalized shouldBe expected
        } catch (e: Throwable) {
            println(normalized)
            throw e
        }
    }
}
