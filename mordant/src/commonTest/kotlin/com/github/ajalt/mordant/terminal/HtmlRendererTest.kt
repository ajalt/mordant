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
        t.print(red("red"))
        t.print("plain")
        t.print(blue("blue"))
    }

    @Test
    @JsName("no_frame")
    fun `no frame`() {
        vt.outputAsHtml(includeFrame = false, backgroundColor = null) shouldBe """
        |<html><body>
        |<pre style="font-family: Menlo, Consolas, Monaco, Liberation Mono, Lucida Console, monospace;"><code>
        |<span style="color: #800000">red</span><span>plain</span><span style="color: #000080">blue</span>
        |</code></pre>
        |</body></html>
        """.trimMargin()
    }

    @Test
    @JsName("frame_no_body_tag")
    fun `frame no body tag`() {
        vt.outputAsHtml(includeBodyTag = false) shouldBe """
        |<pre style="font-family: Menlo, Consolas, Monaco, Liberation Mono, Lucida Console, monospace;background-color: rgb(12 12 12);border-radius: 8px;width: fit-content;padding: 0.5em 1em 0;filter: drop-shadow(0.5em 0.5em 0.5em black);"><code><span style="color: #ff5f56;">⏺ </span><span style="color: #ffbd2e;">⏺ </span><span style="color: #27c93f;">⏺ </span>
        |<span style="color: #800000">red</span><span>plain</span><span style="color: #000080">blue</span>
        |</code></pre>
        """.trimMargin()
    }
}
