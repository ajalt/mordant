package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.rendering.Whitespace.PRE
import com.github.ajalt.mordant.components.Panel
import com.github.ajalt.mordant.components.Text
import kotlin.test.Test

class PanelTest : RenderingTest(width = 20) {

    @Test
    fun `no expand`() = checkRender(Panel(Text("text"), expand = false), """
    ╭────╮
    │text│
    ╰────╯
    """)

    @Test
    fun expand() = checkRender(Panel(Text("text", align = TextAlign.CENTER), expand = true), """
    ╭──────────────────╮
    │       text       │
    ╰──────────────────╯
    """)

    @Test
    fun `no border`() = checkRender(Panel(Text("text\nline 2", whitespace = PRE), borderStyle = null), """
    |text  ⏎
    |line 2⏎
    """.trimMargin())
}
