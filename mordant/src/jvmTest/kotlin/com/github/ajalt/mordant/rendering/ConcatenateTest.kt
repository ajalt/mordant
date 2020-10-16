package com.github.ajalt.mordant.rendering

import org.junit.Test

class ConcatenateTest : RenderingTest() {
    @Test
    fun `three renderables`() = checkRender(Concatenate(
            Text("1"), Text("2"), Text("3")
    ), "1\n2\n3")
}
