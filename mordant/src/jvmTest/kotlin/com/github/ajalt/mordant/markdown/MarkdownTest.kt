package com.github.ajalt.mordant.markdown

import com.github.ajalt.mordant.AnsiColor.brightWhite
import com.github.ajalt.mordant.AnsiColor.gray
import com.github.ajalt.mordant.AnsiLevel
import com.github.ajalt.mordant.AnsiStyle.*
import com.github.ajalt.mordant.Terminal
import com.github.ajalt.mordant.rendering.DEFAULT_THEME
import com.github.ajalt.mordant.rendering.LS
import com.github.ajalt.mordant.rendering.NEL
import com.github.ajalt.mordant.rendering.Theme
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import org.junit.Test

class MarkdownTest {
    @Test
    fun `test paragraphs`() = doTest("""
Paragraph one
wrapped line.

Paragraph 
two
wrapped
line.
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
""", width = 11)

    @Test
    fun `test paragraphs crlf`() = doTest("""
Paragraph one
wrapped line.

Paragraph 
two
wrapped
line.
""".replace("\n", "\r\n"), """
Paragraph one wrapped line.

Paragraph two wrapped line.
""")

    @Test
    fun `test quotes`() = doTest("""
This paragraph
has some "double quotes"
and some 'single quotes'.
""", """
This paragraph has some "double quotes" and some 'single quotes'.
""")

    @Test
    fun `test emphasis`() = doTest("""
An *em span*.

An _em span_.

A **strong span**.

A __strong span__.

A ***strong em span***.

A ~~strikethrough span~~.
""", """
An ${italic("em span")}.

An ${italic("em span")}.

A ${bold("strong span")}.

A ${bold("strong span")}.

A ${(bold + italic)("strong em span")}.

A ${strikethrough("strikethrough span")}.
""")

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

    // https://github.github.com/gfm/#example-306
    @Test
    fun `test unordered list nested`() = doTest("""
- a
  - b
  - c

- d
  - e
  - f
""", """
 • a
    • b
    • c
 • d
    • e
    • f
""")

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
""", width = 11)

    @Test
    fun `test ordered list loose`() = doTest("""
1. a

1. b

1. c
""", """
 1. a
 2. b
 3. c
""")

    @Test
    fun `test ordered list nested`() = doTest("""
1. a
    1. b
    1. c

1. d
    1. e
    1. f
""", """
 1. a
     1. b
     2. c
 2. d
     1. e
     2. f
""")

    @Test
    fun `block quote`() = doTest("""
> line 1
> line 2
>
> line 3
""", """
▎ line 1 line 2
▎
▎ line 3
""")


    // https://github.github.com/gfm/#example-206
    @Test
    fun `block quote with header`() = doTest("""
># Foo
>bar
> baz
""", """
▎
▎ ══ ${bold("Foo")} ═══
▎
▎ bar baz
""", width = 10)

    // https://github.github.com/gfm/#example-208
    @Test
    fun `indented block quote`() = doTest("""
   > # Foo
   > bar
 > baz
""", """
▎
▎ ══ ${bold("Foo")} ═══
▎
▎ bar baz
""", width = 10)


    @Suppress("MarkdownUnresolvedFileReference")
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
    @Suppress("HtmlRequiredAltAttribute", "HtmlDeprecatedAttribute", "HtmlUnknownTarget")
    fun `default html`() = doTest("""
<h1 align="center">
    <img src="example.svg">
    <p>text in html</p>
</h1>
""", """
""")

    @Test
    @Suppress("HtmlRequiredAltAttribute", "HtmlDeprecatedAttribute", "HtmlUnknownTarget")
    fun `show html`() = doTest("""
<h1 align="center">
    <img src="example.svg">
    <p>text in html</p>
</h1>
""", """
<h1 align="center">
    <img src="example.svg">
    <p>text in html</p>
</h1>
""", showHtml = true)

    @Test
    fun `horizontal rule`() = doTest("""
---
""", """
──────────
""", width = 10)

    @Test
    fun `header 1 empty`() = doTest("""
#
""", """


""", width = 19)

    @Test
    fun `header 1`() = doTest("""
# Header Text
""", """

═══ ${bold("Header Text")} ═══

""", width = 19)

    @Test
    fun `header 2`() = doTest("""
## Header Text
""", """

─── ${bold("Header Text")} ───

""", width = 19)

    @Test
    fun `header 3`() = doTest("""
### Header Text
""", """

    ${(bold+underline)("Header Text")}    ⏎

""", width = 19)

    @Test
    fun `header 4`() = doTest("""
#### Header Text
""", """

    ${underline("Header Text")}    ⏎

""", width = 19)

    @Test
    fun `header 5`() = doTest("""
##### Header Text
""", """

    ${italic("Header Text")}    ⏎

""", width = 19)

    @Test
    fun `header 6`() = doTest("""
###### Header Text
""", """

    ${dim("Header Text")}    ⏎

""", width = 19)

    @Test
    fun `header trailing chars`() = doTest("""
# Header Text ##
""", """

═══ ${bold("Header Text")} ═══

""", width = 19)

    @Test
    fun `setext h1`() = doTest("""
Header Text
===========
""", """

═══ ${bold("Header Text")} ═══

""", width = 19)

    @Test
    fun `setext h2`() = doTest("""
  Header Text  
---
""", """

─── ${bold("Header Text")} ───

""", width = 19)

    @Test
    fun `empty code span`() = doTest("""
An `` empty code span.
""", """
An `` empty code span.
""")

    @Test
    fun `code span`() = doTest("""
This is a `code    <br/>`   span.

So `is  this`
span.

A `span
with
a
line break`.
""", """
This is a ${(brightWhite on gray)("code <br/>")} span.

So ${(brightWhite on gray)("is this")} span.

A ${(brightWhite on gray)("span with a line break")}.
""")

// Not supported by the parser yet
// https://spec.commonmark.org/0.29/#backslash-escapes
//    @Test
//    fun `backslash escapes`() = doTest("""
//\!\"\#\${'$'}\%\&\'\(\)\*\+\,\-\.\/\:\;\<\=\>\?\@\[\\\]\^\_\`\{\|\}\~
//""", """
//!"#${'$'}%&'()*+,-./:;&lt;=&gt;?@[\]^_`{|}~
//""")

    @Test
    fun `hard line breaks`() = doTest("""
A hard   
line break with spaces.

A hard\
  line break with a backslash.
  
A *hard   ⏎
line* break with emph.

Code spans `don't\
have` hard breaks.

Also break on ${NEL}NEL and on ${LS}LS$LS
.
""", """
A hard
line break with spaces.

A hard
line break with a backslash.

A ${italic("hard")}
${italic("line")} break with emph.

Code spans ${(brightWhite on gray)("don't\\ have")} hard breaks.

Also break on
NEL and on
LS
.
""")

    // https://github.github.com/gfm/#example-205
    @Test
    fun `header only table`() = doTest("""
| abc | def |
| --- | --- |
""", """
┌─────┬─────┐
│ abc │ def │
└─────┴─────┘
""")

    // https://github.github.com/gfm/#example-198
    @Test
    fun `simple table`() = doTest("""
 | foo | bar |
 | --- | --- |
 | baz | bim |
""", """
┌─────┬─────┐
│ foo │ bar │
├─────┼─────┤
│ baz │ bim │
└─────┴─────┘
""")

    // https://github.github.com/gfm/#example-204
    @Test
    fun `non-rectangular table`() = doTest("""
| abc | def |
| --- | --- |
| bar |
| bar | baz | boo |
""", """
┌─────┬─────┐
│ abc │ def │
├─────┼─────┘
│ bar │      ⏎
├─────┼─────┐
│ bar │ baz │
└─────┴─────┘
""")

    @Test
    fun `table alignment`() = doTest("""
| abc | def | ghi |
| :---: | ---: | :--- |
| bar | baz | quz |
| ...... | ...... | ...... |
""", """
┌────────┬────────┬────────┐
│  abc   │    def │ ghi    │
├────────┼────────┼────────┤
│  bar   │    baz │ quz    │
├────────┼────────┼────────┤
│ ...... │ ...... │ ...... │
└────────┴────────┴────────┘
""")

    // https://github.github.com/gfm/#example-279
    @Test
    fun `task list`() = doTest("""
- [ ] foo
- [x] bar
""", """
 • ☐ foo
 • ☑ bar
""")

    @Test
    fun `indented code block`()= doTest("""
    foo {
        bar
        
        
        
        baz
    }
""", """
┌───────┐
│${(brightWhite on gray)("foo {  ")}│
│${(brightWhite on gray)("    bar")}│
│${(brightWhite on gray)("       ")}│
│${(brightWhite on gray)("       ")}│
│${(brightWhite on gray)("       ")}│
│${(brightWhite on gray)("    baz")}│
│${(brightWhite on gray)("}      ")}│
└───────┘
""")

    @Test
    fun `fenced code block`() = doTest("""
```
foo {
    bar



    baz
}
```
""", """
┌───────┐
│${(brightWhite on gray)("foo {  ")}│
│${(brightWhite on gray)("    bar")}│
│${(brightWhite on gray)("       ")}│
│${(brightWhite on gray)("       ")}│
│${(brightWhite on gray)("       ")}│
│${(brightWhite on gray)("    baz")}│
│${(brightWhite on gray)("}      ")}│
└───────┘
""")

    // https://github.github.com/gfm/#example-113
    @Test
    fun `fenced code block with info string`() = doTest("""
~~~~    ruby startline=3 ${'$'}%@#${'$'}
def foo(x)
  return 3
end
~~~~~~~
""", """
┌──────────┐
│${(brightWhite on gray)("def foo(x)")}│
│${(brightWhite on gray)("  return 3")}│
│${(brightWhite on gray)("end       ")}│
└──────────┘
""")

    @Test
    fun `fenced code block with nbsp`() {
        val nbsp = '\u00A0'
        doTest("""
```
foo${nbsp}bar baz
```
""", """
┌───────────┐
│${(brightWhite on gray)("foo${nbsp}bar baz")}│
└───────────┘
""")
    }

    @Test
    fun `fenced code block with no border in theme`() = doTest("""
```
foo  bar
```
""", """
${(brightWhite on gray)("foo  bar")}
""", theme = object : Theme {
        override val markdownCodeBlockBorder: Boolean get() = false
    })

    @Test
    fun `indented code block with no border in theme`() = doTest("""
    foo  bar
""", """
${(brightWhite on gray)("foo  bar")}
""", theme = object : Theme {
        override val markdownCodeBlockBorder: Boolean get() = false
    })

    private fun doTest(
            @Language("markdown") markdown: String,
            expected: String,
            width: Int = 79,
            showHtml: Boolean = false,
            theme: Theme = DEFAULT_THEME
    ) {
        val md = markdown.replace("⏎", "")
        try {
            val terminal = Terminal(level = AnsiLevel.TRUECOLOR, width = width, theme = theme)
            val actual = terminal.renderMarkdown(md, showHtml)
            try {
                actual shouldBe expected.replace("⏎", "")
            } catch (e: Throwable) {
                println(actual)
                throw e
            }
        } catch (e: Throwable) {
            // Print parse tree on test failure
            val flavour: MarkdownFlavourDescriptor = GFMFlavourDescriptor()
            val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(md)
            println(parsedTree.children.joinToString("\n") { buildString { printNode(md, it) } })
            throw e
        }
    }
}

private fun StringBuilder.printNode(text: String, node: ASTNode, indent: Int = 0) {
    append(" ".repeat(indent * 2))
    append(node.type)
    if (node is LeafASTNode) {
        append(" \'").append(text.substring(node.startOffset, node.endOffset)
                .replace("\r", "␍")
                .replace("\n", "␊")
                .replace(" ", "·")
                .replace("\t", "␉")
                .take(30)
        )
        if (node.endOffset - node.startOffset > 30) append("…")
        append("\'")
    }
    for (c in node.children) {
        append("\n")
        printNode(text, c, indent + 1)
    }
}
