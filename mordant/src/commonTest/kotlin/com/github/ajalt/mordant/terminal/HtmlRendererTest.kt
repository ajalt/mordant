package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.TextColors.*
import io.kotest.matchers.shouldBe
import kotlin.test.Test

@OptIn(ExperimentalTerminalApi::class)
class HtmlRendererTest {
    private val vt = VirtualTerminalInterface()
    private val t = Terminal(terminalInterface = vt)

    @Test
    fun renderAsHtml() {
        t.print(red("red"))
        t.print("plain")
        t.print(blue("blue"))

        vt.outputAsHtml() shouldBe """
        <html><body>
        <pre style="font-family: Menlo, Consolas, Monaco, Liberation Mono, Lucida Console, monospace"><code>
        <span style="color: #800000">red</span><span>plain</span><span style="color: #000080">blue</span>
        </code></pre>
        </body></html>
        """.trimIndent()
    }
}
