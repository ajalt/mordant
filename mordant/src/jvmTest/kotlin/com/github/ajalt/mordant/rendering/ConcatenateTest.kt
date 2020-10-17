package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.AnsiColor.red
import org.junit.Test

class ConcatenateTest : RenderingTest() {
    @Test
    fun `three renderables`() = checkRender(Concatenate(
            Text("1"), Text("2"), Text("3")
    ), "1\n2\n3")

    @Test
    fun `renderable builder`() = checkRender(
            buildRenderable {
                append("1")
                append(Text("2"))
                append("3", style = TextStyle(red))
            }, "1\n2\n${red("3")}"
    )
}
