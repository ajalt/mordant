package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.TextColors.blue
import com.github.ajalt.mordant.rendering.TextColors.red
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

class HtmlRendererTest {
    private val vt = TerminalRecorder()
    private val t = Terminal(terminalInterface = vt)

    init {
        t.print(red("red red"))
        t.println("plain")
        t.print(blue("blue blue"))
    }

    @Test
    @JsName("no_frame")
    fun `no frame`() {
        vt.outputAsHtml(backgroundColor = null) shouldBe """
        |<html><body>
        |<pre style="font-family: monospace"><code>
        |<span style="color: #800000">red red</span><span>plain</span>
        |<span style="color: #000080">blue blue</span>
        |</code></pre>
        |</body></html>
        """.trimMargin()
    }

    @Test
    @JsName("frame_no_body_tag")
    fun `frame no body tag`() {
        vt.outputAsHtml(includeCodeTag = false, includeBodyTag = false) shouldBe """
        |<div style="border-radius: 8px;width: fit-content;padding: 0.5em 1em;filter: drop-shadow(0.5em 0.5em 0.5em black);background-color: rgb(12 12 12);">\n<div style="margin: -0.75em 0px;font-size: 2em"><span style="color: #ff5f56;">⏺&nbsp;</span><span style="color: #ffbd2e;">⏺&nbsp;</span><span style="color: #27c93f;">⏺&nbsp;</span></div>
        |<pre style="font-family: monospace">
        |<span style="color: #800000">red red</span><span>plain</span>
        |<span style="color: #000080">blue blue</span>
        |</pre>
        |</div>
        """.trimMargin()
    }
}
