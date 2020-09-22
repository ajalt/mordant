package com.github.ajalt.mordant.rendering

import org.junit.Test

class PanelTest : RenderingTest(width = 20) {

    @Test
    fun `no expand`() = checkRender(Panel(Text("text"), expand = false), """
    |┌────┐
    |│text│
    |└────┘
    """)

    @Test
    fun expand() = checkRender(Panel(Text("text"), expand = true), """
    |┌──────────────────┐
    |│       text       │
    |└──────────────────┘
    """)
}
