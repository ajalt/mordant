package com.github.ajalt.mordant.rendering.components

import com.github.ajalt.mordant.rendering.RenderingTest
import com.github.ajalt.mordant.rendering.Whitespace.PRE
import com.github.ajalt.mordant.widgets.DefinitionListBuilder
import com.github.ajalt.mordant.widgets.Panel
import com.github.ajalt.mordant.widgets.Text
import com.github.ajalt.mordant.widgets.definitionList
import org.junit.Test

class DefinitionListTest : RenderingTest() {

    @Test
    fun `multiple items inline`() = doTest("""
term 1:      desc 1
term 2 2:    desc 2 2
    """) {
        entry("term 1:", "desc 1")
        entry("term 2 2:", "desc 2 2")
        inline = true
        descriptionSpacing = 4
    }

    @Test
    fun `inline desc wrap`() = doTest("""
term 1:    Lorem ipsum
           dolor sit
           amet
term 2 2:  desc 2 2
   """, width = 23) {
        entry("term 1:", "Lorem ipsum dolor sit amet")
        entry("term 2 2:", "desc 2 2")
        inline = true
    }

    @Test
    fun `inline desc no wrap due to short desc`() = doTest("""
term 1 lorem ipsum:  1
term 2 2:            2
   """, width = 23) {
        entry("term 1 lorem ipsum:", "1")
        entry("term 2 2:", "2")
        inline = true
    }

    @Test
    fun `inline term overflow`() = doTest("""
term 1:   desc 1
term 2 lorem ipsum:
          dolor sit amet
term 3::  desc 3
   """, width = 24) {
        entry("term 1:", "desc 1")
        entry("term 2 lorem ipsum:", "dolor sit amet")
        entry("term 3::", "desc 3")
        inline = true
    }

    @Test
    fun `inline term all overflow`() = doTest("""
term 1 lorem ipsum:
    dolor sit amet
term 2 lorem ipsum:
    dolor sit amet
   """, width = 24) {
        inline = true
        entry("term 1 lorem ipsum:", "dolor sit amet")
        entry("term 2 lorem ipsum:", "dolor sit amet")
    }

    @Test
    fun `inline term overflow wrap`() = doTest("""
term 1:  desc 1
term 2 lorem ipsum
dolor sit amet:
         desc 2
term 3:  desc 3
   """, width = 21) {
        inline = true

        entry("term 1:", "desc 1")
        entry("term 2 lorem ipsum dolor sit amet:", "desc 2")
        entry("term 3:", "desc 3")
    }

    @Test
    fun `inline multi line term and desc`() = doTest("""
term 1:   desc 1
.
.
term 2:   desc 2
          .
          .
╭──────╮  ╭──────╮
│term 3│  │desc 3│
╰──────╯  ╰──────╯
   """, width = 21) {
        inline = true

        entry(Text("term 1:\n.\n.", whitespace = PRE), "desc 1")
        entry("term 2:", Text("desc 2\n.\n.", whitespace = PRE))
        entry(Panel("term 3"), Panel("desc 3"))
    }


    @Test
    fun `non-inline`() = doTest("""
term 1:
desc 1

term 2 lorem ipsum
dolor sit amet:
desc 2

term 3:
desc 3 consectetur
adipiscing elit
""", width = 21) {
        entry("term 1:", "desc 1")
        entry {
            term(Text("term 2 lorem ipsum dolor sit amet:"))
            description(Text("desc 2"))
        }
        entry {
            term("term 3:")
            description("desc 3 consectetur adipiscing elit")
        }
    }

    @Test
    fun `non-inline custom spacing`() = doTest("""
term 1:

desc 1
term 2:

desc 2
term 3:

desc 3
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
