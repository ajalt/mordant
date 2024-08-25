package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.widgets.SelectList.Entry
import kotlin.js.JsName
import kotlin.test.Test

class SelectListTest : RenderingTest() {
    @[Test JsName("no_optional_elements")]
    fun `no optional elements`() = doTest(
        """
    ░foo
    ░bar
    ░baz
    """,
        Entry("foo"),
        Entry("bar"),
        Entry("baz"),
        selectedMarker = "",
        cursorMarker = "",
    )

    @[Test JsName("no_selected_marker")]
    fun `no selected marker`() = doTest(
        """
    ░title
    ░  foo
    ░  bar
    ░❯ baz
    """,
        Entry("foo"),
        Entry("bar"),
        Entry("baz"),
        title = Text("title"),
        cursorIndex = 2,
        selectedMarker = "",
    )

    @[Test JsName("multi_selected_marker")]
    fun `multi selected marker`() = doTest(
        """
    ░title
    ░❯ • foo
    ░  ✓ bar
    ░  • baz
    ░  ✓ qux
    ░caption
    """,
        Entry("foo"),
        Entry("bar", selected = true),
        Entry("baz"),
        Entry("qux", selected = true),
        title = Text("title"),
        cursorIndex = 0,
        captionBottom = Text("caption"),
    )

    @[Test JsName("styles_with_descriptions")]
    fun `styles with descriptions`() = doTest(
        """
    ░  ${blue("•")} ${red("foo")}    ░
    ░    desc1  ░
    ░      line2
    ░      line3
    ░${magenta("❯")} ${green("✓")} ${green("bar")}    ░
    ░    desc2  ░
    ░  ${blue("•")} ${red("baz")}    ░
    """,
        Entry("foo", description = "desc1\n  line2\n  line3"),
        Entry("bar", selected = true, description = "desc2"),
        Entry("baz"),
        cursorIndex = 1,
        selectedStyle = green,
        cursorStyle = magenta,
        unselectedTitleStyle = red,
        unselectedMarkerStyle = blue,
    )

    private fun doTest(
        expected: String,
        vararg entries: Entry,
        title: Widget? = null,
        cursorIndex: Int = -1,
        cursorMarker: String = "❯",
        selectedMarker: String = "✓",
        unselectedMarker: String = "•",
        captionBottom: Widget? = null,
        selectedStyle: TextStyle = TextStyle(),
        cursorStyle: TextStyle = TextStyle(),
        unselectedTitleStyle: TextStyle = TextStyle(),
        unselectedMarkerStyle: TextStyle = TextStyle(),
    ) = checkRender(
        SelectList(
            entries = entries.toList(),
            title = title,
            cursorIndex = cursorIndex,
            cursorMarker = cursorMarker,
            selectedMarker = selectedMarker,
            unselectedMarker = unselectedMarker,
            captionBottom = captionBottom,
            selectedStyle = selectedStyle,
            cursorStyle = cursorStyle,
            unselectedTitleStyle = unselectedTitleStyle,
            unselectedMarkerStyle = unselectedMarkerStyle,
        ), expected
    )
}
