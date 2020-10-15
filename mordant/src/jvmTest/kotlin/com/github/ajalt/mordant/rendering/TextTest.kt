package com.github.ajalt.mordant.rendering

import org.junit.Test


class TextTest : RenderingTest() {
    @Test
    fun `override width`() = checkRender(Text("""
    Lorem ipsum dolor
    sit amet
    """, width = 12), """
    Lorem ipsum
    dolor sit
    amet
    """, width = 79)
}
