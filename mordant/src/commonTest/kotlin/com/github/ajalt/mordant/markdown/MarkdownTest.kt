package com.github.ajalt.mordant.markdown

import com.github.ajalt.mordant.terminal.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TextColors.*
import com.github.ajalt.mordant.terminal.TextStyles.*
import com.github.ajalt.mordant.terminal.TextStyles.Companion.hyperlink
import com.github.ajalt.mordant.rendering.DEFAULT_THEME
import com.github.ajalt.mordant.components.LS
import com.github.ajalt.mordant.components.NEL
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.internal.generateHyperlinkId
import io.kotest.matchers.shouldBe
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import kotlin.test.Test

private val code = (brightRed on gray) + dim

class MarkdownTest {
    init {
        generateHyperlinkId = { "x" }
    }

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
${brightYellow("▎ line 1 line 2")}
${brightYellow("▎")}
${brightYellow("▎ line 3")}
""")


    // https://github.github.com/gfm/#example-206
    @Test
    fun `block quote with header`() = doTest("""
># Foo
>bar
> baz
""", """
${brightYellow("▎")}
${brightYellow("▎ ══ ${bold("Foo")} ═══")}
${brightYellow("▎")}
${brightYellow("▎ bar baz")}
""", width = 10)

    // https://github.github.com/gfm/#example-208
    @Test
    fun `indented block quote`() = doTest("""
   > # Foo
   > bar
 > baz
""", """
${brightYellow("▎")}
${brightYellow("▎ ══ ${(bold)("Foo")} ═══")}
${brightYellow("▎")}
${brightYellow("▎ bar baz")}
""", width = 10)


    @Suppress("MarkdownUnresolvedFileReference")
    @Test
    fun `visible links`() = doTest("""
[a reference link][a link]

[a link]

[inline link 1]( example.com "with a title" )

[inline link 2](http://www.example.com)

[inline link 3]()

[inline link 4](<>)

[inline link 5](</my uri>)

![an image]( example.com "with a title" )

<https://example.com/autolink>

www.example.com/url

<autolink@example.com>

[a link]: example.com
""", """
${blue("${brightBlue("a reference link")}[a link]")}

${blue("[a link]")}

${blue("${brightBlue("inline link 1")}(example.com)")}

${blue("${brightBlue("inline link 2")}(http://www.example.com)")}

${blue("${brightBlue("inline link 3")}()")}

${blue("${brightBlue("inline link 4")}()")}

${blue("${brightBlue("inline link 5")}(/my uri)")}

${blue("${brightBlue("an image")}(example.com)")}

${brightBlue("https://example.com/autolink")}

${brightBlue("www.example.com/url")}

<${brightBlue("autolink@example.com")}>

${blue("[a link]: example.com")}
""")

    @Suppress("MarkdownUnresolvedFileReference")
    @Test
    fun `link with hyperlinks`() = doTest("""
[a reference link][a link]

[a link]

[inline link 1]( example.com/1 "with a title" )

[inline link 2](http://www.example.com/2)

[inline link 3]()

[inline link 4](<>)

[inline link 5](</my uri>)

![an image]( example.com/#3 "with a title" )

<https://example.com/autolink>

www.example.com/url

<autolink@example.com>

[a link]: example.com/4
""", """
${(brightBlue + hyperlink("example.com/4"))("a reference link")}

${(brightBlue + hyperlink("example.com/4"))("a link")}

${(brightBlue + hyperlink("example.com/1"))("inline link 1")}

${(brightBlue + hyperlink("http://www.example.com/2"))("inline link 2")}

${blue("${brightBlue("inline link 3")}()")}

${blue("${brightBlue("inline link 4")}()")}

${(brightBlue + hyperlink("/my uri"))("inline link 5")}

${(brightBlue + hyperlink("example.com/#3"))("an image")}

${brightBlue("https://example.com/autolink")}

${brightBlue("www.example.com/url")}

<${brightBlue("autolink@example.com")}>


""", hyperlinks = true)

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
    fun `default html inline`() = doTest("""
Hello <b>world</b>.
""", """
Hello world.
""")

    @Test
    fun `show html inline`() = doTest("""
Hello <b>world</b>.
""", """
Hello <b>world</b>.
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

${magenta("═══ ${bold("Header Text")} ═══")}

""", width = 19)

    @Test
    fun `header 2`() = doTest("""
## Header Text
""", """

${magenta("─── ${bold("Header Text")} ───")}

""", width = 19)

    @Test
    fun `header 3`() = doTest("""
### Header Text
""", """

${magenta("    ${(bold + underline)("Header Text")}    ")}

""", width = 19)

    @Test
    fun `header 4`() = doTest("""
#### Header Text
""", """

${magenta("    ${underline("Header Text")}    ")}

""", width = 19)

    @Test
    fun `header 5`() = doTest("""
##### Header Text
""", """

${magenta("    ${italic("Header Text")}    ")}

""", width = 19)

    @Test
    fun `header 6`() = doTest("""
###### Header Text
""", """

${magenta("    ${dim("Header Text")}    ")}

""", width = 19)

    @Test
    fun `header trailing chars`() = doTest("""
# Header Text ##
""", """

${magenta("═══ ${bold("Header Text")} ═══")}

""", width = 19)

    @Test
    fun `setext h1`() = doTest("""
Header Text
===========
""", """

${magenta("═══ ${bold("Header Text")} ═══")}

""", width = 19)

    @Test
    fun `setext h2`() = doTest("""
  Header Text  
---
""", """

${magenta("─── ${bold("Header Text")} ───")}

""", width = 19)

    @Test
    fun `empty code span`() = doTest("""
An `` empty code span.
""", """
An `` empty code span.
""")

    @Test
    fun `code span`() = doTest("""
This `is ` a `code    <br/>`   `span`.

So `is  this`
span.

A `span
with
a
line break`.
""", """
This ${code("is")} a ${code("code <br/>")} ${code("span")}.

So ${code("is this")} span.

A ${code("span with a line break")}.
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

Code spans ${code("don't\\ have")} hard breaks.

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
│${bold(" abc ")}│${bold(" def ")}│
╘═════╧═════╛
""")

    // https://github.github.com/gfm/#example-198
    @Test
    fun `simple table`() = doTest("""
 | foo | bar |
 | --- | --- |
 | baz | bim |
""", """
┌─────┬─────┐
│${bold(" foo ")}│${bold(" bar ")}│
╞═════╪═════╡
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
│${bold(" abc ")}│${bold(" def ")}│
╞═════╪═════╛
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
│${bold("  abc   ")}│${bold("    def ")}│${bold(" ghi    ")}│
╞════════╪════════╪════════╡
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
╭───────╮
│${brightRed("foo {  ")}│
│${brightRed("    bar")}│
│${brightRed("       ")}│
│${brightRed("       ")}│
│${brightRed("       ")}│
│${brightRed("    baz")}│
│${brightRed("}      ")}│
╰───────╯
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
╭───────╮
│${brightRed("foo {  ")}│
│${brightRed("    bar")}│
│${brightRed("       ")}│
│${brightRed("       ")}│
│${brightRed("       ")}│
│${brightRed("    baz")}│
│${brightRed("}      ")}│
╰───────╯
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
╭──────────╮
│${brightRed("def foo(x)")}│
│${brightRed("  return 3")}│
│${brightRed("end       ")}│
╰──────────╯
""")

    @Test
    fun `fenced code block with nbsp`() {
        val nbsp = '\u00A0'
        doTest("""
```
foo${nbsp}bar baz
```
""", """
╭───────────╮
│${brightRed("foo${nbsp}bar baz")}│
╰───────────╯
""")
    }

    @Test
    fun `fenced code block with no border in theme`() = doTest("""
```
foo  bar
```
""", """
${brightRed("foo  bar")}
""", theme = object : Theme {
        override val markdownCodeBlockBorder: Boolean get() = false
    })

    @Test
    fun `indented code block with no border in theme`() = doTest("""
    foo  bar
""", """
${brightRed("foo  bar")}
""", theme = object : Theme {
        override val markdownCodeBlockBorder: Boolean get() = false
    })

    private fun doTest(
            /* TODO: @Language("markdown")*/ markdown: String,
            expected: String,
            width: Int = 79,
            showHtml: Boolean = false,
            theme: Theme = DEFAULT_THEME,
            hyperlinks: Boolean = false
    ) {
        val md = markdown.replace("⏎", "")
        try {
            val terminal = Terminal(
                    ansiLevel = AnsiLevel.TRUECOLOR,
                    width = width,
                    theme = theme,
                    hyperlinks = hyperlinks
            )
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
