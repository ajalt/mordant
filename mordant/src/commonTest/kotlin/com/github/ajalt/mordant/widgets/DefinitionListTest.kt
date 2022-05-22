package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.Whitespace.NORMAL
import com.github.ajalt.mordant.rendering.Whitespace.PRE
import com.github.ajalt.mordant.test.RenderingTest
import com.github.ajalt.mordant.widgets.DefinitionListBuilder
import com.github.ajalt.mordant.widgets.Panel
import com.github.ajalt.mordant.widgets.Text
import com.github.ajalt.mordant.widgets.definitionList
import kotlin.js.JsName
import kotlin.test.Test

class DefinitionListTest : RenderingTest() {

    @Test
    @JsName("multiple_items_inline")
    fun `multiple items inline`() = doTest("""
    ░term 1:      desc 1
    ░term 2 2:    desc 2 2
    """) {
        entry("term 1:", "desc 1")
        entry("term 2 2:", "desc 2 2")
        inline = true
        descriptionSpacing = 4
    }

    @Test
    @JsName("inline_desc_wrap")
    fun `inline desc wrap`() = doTest("""
    ░term 1:    Lorem ipsum
    ░           dolor sit
    ░           amet
    ░term 2 2:  desc 2 2
    """, width = 23) {
        entry("term 1:", Text("Lorem ipsum dolor sit amet", whitespace = NORMAL))
        entry("term 2 2:", "desc 2 2")
        inline = true
    }

    @Test
    @JsName("inline_desc_no_wrap_due_to_short_desc")
    fun `inline desc no wrap due to short desc`() = doTest("""
    ░term 1 lorem ipsum:  1
    ░term 2 2:            2
    """, width = 23) {
        entry("term 1 lorem ipsum:", "1")
        entry("term 2 2:", "2")
        inline = true
    }

    @Test
    @JsName("inline_term_overflow")
    fun `inline term overflow`() = doTest("""
    ░term 1:   desc 1
    ░term 2 lorem ipsum:
    ░          dolor sit amet
    ░term 3::  desc 3
    """, width = 24) {
        entry("term 1:", "desc 1")
        entry("term 2 lorem ipsum:", "dolor sit amet")
        entry("term 3::", "desc 3")
        inline = true
    }

    @Test
    @JsName("inline_term_all_overflow")
    fun `inline term all overflow`() = doTest("""
    ░term 1 lorem ipsum:
    ░    dolor sit amet
    ░term 2 lorem ipsum:
    ░    dolor sit amet
    """, width = 24) {
        inline = true
        entry("term 1 lorem ipsum:", "dolor sit amet")
        entry("term 2 lorem ipsum:", "dolor sit amet")
    }

    @Test
    @JsName("inline_term_overflow_wrap")
    fun `inline term overflow wrap`() = doTest("""
    ░term 1:  desc 1
    ░term 2 lorem ipsum
    ░dolor sit amet:
    ░         desc 2
    ░term 3:  desc 3
    """, width = 21) {
        inline = true

        entry("term 1:", "desc 1")
        entry(Text("term 2 lorem ipsum dolor sit amet:", whitespace = NORMAL), "desc 2")
        entry("term 3:", "desc 3")
    }

    @Test
    @JsName("inline_multi_line_term_and_desc")
    fun `inline multi line term and desc`() = doTest("""
   ░term 1:   desc 1
   ░.
   ░.
   ░term 2:   desc 2
   ░          .
   ░          .
   ░╭──────╮  ╭──────╮
   ░│term 3│  │desc 3│
   ░╰──────╯  ╰──────╯
   """, width = 21) {
        inline = true

        entry(Text("term 1:\n.\n.", whitespace = PRE), "desc 1")
        entry("term 2:", Text("desc 2\n.\n.", whitespace = PRE))
        entry(Panel("term 3"), Panel("desc 3"))
    }


    @Test
    @JsName("non_inline")
    fun `non-inline`() = doTest("""
    ░term 1:
    ░desc 1
    ░
    ░term 2 lorem ipsum
    ░dolor sit amet:
    ░desc 2
    ░
    ░term 3:
    ░desc 3 consectetur
    ░adipiscing elit
    """, width = 21) {
        entry("term 1:", "desc 1")
        entry {
            term(Text("term 2 lorem ipsum dolor sit amet:", whitespace = NORMAL))
            description(Text("desc 2"))
        }
        entry {
            term("term 3:")
            description("desc 3 consectetur adipiscing elit", whitespace = NORMAL)
        }
    }

    @Test
    @JsName("non_inline_custom_spacing")
    fun `non-inline custom spacing`() = doTest("""
    ░term 1:
    ░
    ░desc 1
    ░term 2:
    ░
    ░desc 2
    ░term 3:
    ░
    ░desc 3
    """, width = 21) {
        entrySpacing = 0
        descriptionSpacing = 1
        entry("term 1:", "desc 1")
        entry("term 2:", "desc 2")
        entry("term 3:", "desc 3")
    }

    private fun doTest(expected: String, width: Int = 79, init: DefinitionListBuilder.() -> Unit) {
        checkRender(definitionList(init), expected, width = width)
    }
}
