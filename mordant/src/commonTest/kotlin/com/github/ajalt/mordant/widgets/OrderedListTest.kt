package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.test.RenderingTest
import kotlin.js.JsName
import kotlin.test.Test

class OrderedListTest : RenderingTest() {
    @Test
    @JsName("vararg_string_constructor")
    fun `vararg string constructor`() = checkRender(
        OrderedList("one", "two", "three"),
        """
        ░ 1. one
        ░ 2. two
        ░ 3. three
        """
    )

    @Test
    @JsName("vararg_widget_constructor")
    fun `vararg widget constructor`() = checkRender(
        OrderedList(Text("one"), Text("two"), Text("three")),
        """
        ░ 1. one
        ░ 2. two
        ░ 3. three
        """
    )

    @Test
    @JsName("empty_list")
    fun `empty list`() = checkRender(
        OrderedList(emptyList()),
        ""
    )
}
