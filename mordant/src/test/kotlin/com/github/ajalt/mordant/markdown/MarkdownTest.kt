package com.github.ajalt.mordant.markdown

import com.github.ajalt.mordant.Terminal
import com.github.ajalt.mordant.rendering.markdown.MarkdownRenderer
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import kotlin.test.Test

class MarkdownTest {
    @Test
    fun `test paragraphs`() = doTest("""
Paragraph one
wrapped line.

Paragraph two
wrapped line.
""", """
Paragraph one wrapped line.

Paragraph two wrapped line.
""")

    @Test
    fun `test paragraphs wrapped`() = doTest("""
Paragraph one
wrapped line.

Paragraph two
wrapped line.
""", """
Paragraph
one wrapped
line.

Paragraph
two wrapped
line.
""", width = 10)

    @Test
    fun `test unordered list`() = doTest("""
- line 1
- line 2a
  line 2b
- line 3
""", """
 • line 1
 • line 2a line 2b
 • line 3
""")

    @Test
    fun `test unordered list wrap`() = doTest("""
- line 1
- line 2a
  line 2b
- line 3
""", """
 • line 1
 • line 2a
   line 2b
 • line 3
""", width = 10)

    @Test
    fun `test ordered list`() = doTest("""
1. line 1
1. line 2a
   line 2b
1. line 3
""", """
 1. line 1
 2. line 2a line 2b
 3. line 3
""")

    @Test
    fun `test ordered list wrap`() = doTest("""
1. line 1
1. line 2a
  line 2b
1. line 3
""", """
 1. line 1
 2. line 2a
    line 2b
 3. line 3
""", width = 10)

    @Test
    fun `block quote`() = doTest("""
> line 1
>
> line 2
""", """
▎ line 1
▎
▎ line 2
""")

    @Test
    fun `various links`() = doTest("""
[a reference link][a link]

[a link]

[an inline link](example.com "with a title")

![an image](example.com "with a title")

<https://example.com/autolink>

www.example.com/url

<autolink@example.com>

[a link]: example.com
""", """
a reference link

a link

an inline link

an image

https://example.com/autolink

www.example.com/url

<autolink@example.com>


""")

    @Test
    fun `raw html`() = doTest("""
<h1 align="center">
    <img src="example.svg">
    <p>text in html</p>
</h1>
""", """
<h1 align="center">
    <img src="example.svg">
    <p>text in html</p>
</h1>
""")


    @Test
    fun `horizontal rule`() = doTest("""
---
""", """
──────────
""", width=10)




    private fun doTest(@Language("markdown") markdown: String, expected: String, width: Int = 79) {
        val terminal = Terminal(width = width)
        val actual = terminal.renderMarkdown(markdown)
        actual shouldBe expected
    }
}
