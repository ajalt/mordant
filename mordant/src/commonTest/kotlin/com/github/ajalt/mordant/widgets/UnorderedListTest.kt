package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.test.RenderingTest
import kotlin.js.JsName
import kotlin.test.Test

class UnorderedListTest : RenderingTest() {
    @Test
    @JsName("vararg_string_constructor")
    fun `vararg string constructor`() = checkRender(
        UnorderedList("one", "two", "three"),
        """
        ░ • one
        ░ • two
        ░ • three
        """
    )

    @Test
    @JsName("vararg_widget_constructor")
    fun `vararg widget constructor`() = checkRender(
        UnorderedList(Text("one"), Text("two"), Text("three")),
        """
        ░ • one
        ░ • two
        ░ • three
        """
    )

    @Test
    @JsName("empty_list")
    fun `empty list`() = checkRender(
        UnorderedList(emptyList()),
        ""
    )
}
