package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.rendering.Whitespace.PRE
import com.github.ajalt.mordant.components.DefinitionList
import com.github.ajalt.mordant.components.Panel
import com.github.ajalt.mordant.components.Text
import kotlin.test.Test

class DefinitionListTest : RenderingTest() {

    @Test
    fun `multiple items inline`() = checkRender(DefinitionList(
            "term 1:" to "desc 1",
            "term 2 2:" to "desc 2 2",
            inline = true
    ), """
term 1:    desc 1
term 2 2:  desc 2 2
    """)

    @Test
    fun `inline desc wrap`() = checkRender(DefinitionList(
            "term 1:" to "Lorem ipsum dolor sit amet",
            "term 2 2:" to "desc 2 2",
            inline = true
    ), """
term 1:    Lorem ipsum
           dolor sit
           amet
term 2 2:  desc 2 2
   """, width = 23)

    @Test
    fun `inline desc no wrap due to short desc`() = checkRender(DefinitionList(
            "term 1 lorem ipsum:" to "1",
            "term 2 2:" to "2",
            inline = true
    ), """
term 1 lorem ipsum:  1
term 2 2:            2
   """, width = 23)

    @Test
    fun `inline term overflow`() = checkRender(DefinitionList(
            "term 1:" to "desc 1",
            "term 2 lorem ipsum:" to "dolor sit amet",
            "term 3::" to "desc 3",
            inline = true
    ), """
term 1:   desc 1
term 2 lorem ipsum:
          dolor sit amet
term 3::  desc 3
   """, width = 24)

    @Test
    fun `inline term all overflow`() = checkRender(DefinitionList(
            "term 1 lorem ipsum:" to "dolor sit amet",
            "term 2 lorem ipsum:" to "dolor sit amet",
            inline = true
    ), """
term 1 lorem ipsum:
    dolor sit amet
term 2 lorem ipsum:
    dolor sit amet
   """, width = 24)

    @Test
    fun `inline term overflow wrap`() = checkRender(DefinitionList(
            "term 1:" to "desc 1",
            "term 2 lorem ipsum dolor sit amet:" to "desc 2",
            "term 3:" to "desc 3",
            inline = true
    ), """
term 1:  desc 1
term 2 lorem ipsum
dolor sit amet:
         desc 2
term 3:  desc 3
   """, width = 21)

    @Test
    fun `inline multi line term and desc`() = checkRender(DefinitionList(
            mapOf(
                    Text("term 1:\n.\n.", whitespace = PRE) to Text("desc 1"),
                    Text("term 2:") to Text("desc 2\n.\n.", whitespace = PRE),
                    Panel("term 3") to Panel("desc 3")
            ),
            inline = true
    ), """
term 1:   desc 1
.
.
term 2:   desc 2
          .
          .
╭──────╮  ╭──────╮
│term 3│  │desc 3│
╰──────╯  ╰──────╯
   """, width = 21)


    @Test
    fun `non-inline`() = checkRender(DefinitionList(
            "term 1:" to "desc 1",
            "term 2 lorem ipsum dolor sit amet:" to "desc 2",
            "term 3:" to "desc 3 consectetur adipiscing elit",
            inline = false
    ), """
term 1:
         desc 1
term 2 lorem ipsum
dolor sit amet:
         desc 2
term 3:
         desc 3
         consectetur
         adipiscing
         elit
   """, width = 21)
}
