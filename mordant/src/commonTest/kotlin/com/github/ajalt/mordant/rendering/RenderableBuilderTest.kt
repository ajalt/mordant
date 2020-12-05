package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.components.Text
import com.github.ajalt.mordant.terminal.TextColors.red
import kotlin.test.Test

class RenderableBuilderTest : RenderingTest() {
    @Test
    fun `renderable builder`() = checkRender(
            buildRenderable {
                appendln("1")
                appendln()
                appendAll(listOf(Text("2"), Text("3")))
                appendln("4", style = TextStyle(red))
            }, "1\n\n2\n3\n${red("4")}"
    )
}
