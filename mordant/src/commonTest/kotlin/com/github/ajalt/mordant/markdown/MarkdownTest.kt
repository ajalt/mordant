package com.github.ajalt.mordant.markdown

import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.github.ajalt.mordant.rendering.TextStyles.Companion.hyperlink
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.test.normalizeHyperlinks
import com.github.ajalt.mordant.widgets.LS
import com.github.ajalt.mordant.widgets.NEL
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import kotlin.js.JsName
import kotlin.test.Test

// Header rules are styled by removing the bold from the theme style
private val TextStyle.colorOnly get() = TextStyle(color, bgColor)

class MarkdownTest {
    private val quote = Theme.Default.style("markdown.blockquote")
    private val code = Theme.Default.style("markdown.code.span")
    private val codeBlock = Theme.Default.style("markdown.code.block")
    private val h1 = Theme.Default.style("markdown.h1")
    private val h2 = Theme.Default.style("markdown.h2")
    private val h3 = Theme.Default.style("markdown.h3")
    private val h4 = Theme.Default.style("markdown.h4")
    private val h5 = Theme.Default.style("markdown.h5")
    private val h6 = Theme.Default.style("markdown.h6")
    private val linkText = Theme.Default.style("markdown.link.text")
    private val linkDest = Theme.Default.style("markdown.link.destination")
    private val imgAlt = Theme.Default.style("markdown.img.alt-text")

    @Test
    @JsName("default_style_is_colored")
    fun `default style is colored`() = forAll(
        row(quote),
        row(quote),
        row(code),
        row(codeBlock),
        row(h1),
        row(h2),
        row(h3),
        row(h4),
        row(h5),
        row(h6),
        row(linkText),
        row(linkDest),
        row(imgAlt),
    ) { it shouldNotBe DEFAULT_STYLE }

    @Test
    @JsName("test_paragraphs")
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
    @JsName("test_paragraphs_wrapped")
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
    @JsName("test_paragraphs_crlf")
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
    @JsName("test_quotes")
    fun `test quotes`() = doTest("""
This paragraph
has some "double quotes"
and some 'single quotes'.
""", """
This paragraph has some "double quotes" and some 'single quotes'.
""")

    @Test
    @JsName("test_emphasis")
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
    @JsName("test_unordered_list")
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
    @JsName("test_unordered_list_wrap")
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
    @JsName("test_unordered_list_nested")
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
    @JsName("test_ordered_list")
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
    @JsName("test_ordered_list_wrap")
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
    @JsName("test_ordered_list_loose")
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
    @JsName("test_ordered_list_nested")
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
    @JsName("block_quote")
    fun `block quote`() = doTest("""
> line 1
> line 2
>
> line 3
""", """
${quote("▎ line 1 line 2")}
${quote("▎")}
${quote("▎ line 3")}
""")


    // https://github.github.com/gfm/#example-206
    @Test
    @JsName("block_quote_with_header")
    fun `block quote with header`() = doTest("""
># Foo
>bar
> baz
""", """
${quote("▎")}
${quote("▎ ══ ${bold("Foo")} ═══")}
${quote("▎")}
${quote("▎ bar baz")}
""", width = 10)

    // https://github.github.com/gfm/#example-208
    @Test
    @JsName("indented_block_quote")
    fun `indented block quote`() = doTest("""
   > # Foo
   > bar
 > baz
""", """
${quote("▎")}
${quote("▎ ══ ${(bold)("Foo")} ═══")}
${quote("▎")}
${quote("▎ bar baz")}
""", width = 10)


    @Suppress("MarkdownUnresolvedFileReference")
    @Test
    @JsName("visible_links")
    fun `visible links`() = doTest("""
[a reference link][a link]

[a link]

[inline link 1]( example.com "with a title" )

[inline link 2](https://www.example.com)

[inline link 3]()

[inline link 4](<>)

[inline link 5](</my uri>)

<https://example.com/autolink>

www.example.com/url

<autolink@example.com>

[a link]: example.com
""", """
${linkText("a reference link${linkDest("[a link]")}")}

${linkDest("[a link]")}

${linkText("inline link 1${linkDest("(example.com)")}")}

${linkText("inline link 2${linkDest("(https://www.example.com)")}")}

${linkText("inline link 3${linkDest("()")}")}

${linkText("inline link 4${linkDest("()")}")}

${linkText("inline link 5${linkDest("(/my uri)")}")}

${linkText("https://example.com/autolink")}

${linkText("www.example.com/url")}

<${linkText("autolink@example.com")}>

${linkDest("[a link]: example.com")}
""")

    @Suppress("MarkdownUnresolvedFileReference")
    @Test
    @JsName("link_with_hyperlinks")
    fun `link with hyperlinks`() = doTest("""
[a reference link][a link]

[a link]

[a link][]

[inline link 1]( example.com/1 "with a title" )

[inline link 2](https://www.example.com/2)

[inline link 3]()

[inline link 4](<>)

[inline link 5](</my uri>)

<https://example.com/autolink>

www.example.com/url

<autolink@example.com>

[a link]: example.com/4
""", """
${(linkText + hyperlink("example.com/4"))("a reference link")}

${(linkText + hyperlink("example.com/4"))("a link")}

${(linkText + hyperlink("example.com/4"))("a link")}

${(linkText + hyperlink("example.com/1"))("inline link 1")}

${(linkText + hyperlink("https://www.example.com/2"))("inline link 2")}

${linkText("inline link 3${linkDest("()")}")}

${linkText("inline link 4${linkDest("()")}")}

${(linkText + hyperlink("/my uri"))("inline link 5")}

${linkText("https://example.com/autolink")}

${linkText("www.example.com/url")}

<${linkText("autolink@example.com")}>


""".normalizeHyperlinks(), hyperlinks = true)

    @Test
    @JsName("image_tags")
    fun `image tags`() = doTest("""
![an image]( example.png "a title" )

![](example.png "a title")

![an image](example.png)

![](example.png)
""", """
🖼️ ${imgAlt("an image")}



🖼️ ${imgAlt("an image")}


""")

    @Test
    @JsName("md_in_link_titles")
    fun `md in link titles`() = doTest("""
[`code`](example.com/1)

[![an image](img.png)](example.com/2)
""", """
${(linkText + hyperlink("example.com/1"))("code")}

${(linkText + hyperlink("example.com/2"))("🖼️ an image")}
""".normalizeHyperlinks(), hyperlinks = true)

    @Test
    @JsName("default_html")
    fun `default html`() = doTest("""
<h1 align="center">
    <img src="example.svg">
    <p>text in html</p>
</h1>
""", """
""")

    @Test
    @JsName("show_html")
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
    @JsName("default_html_inline")
    fun `default html inline`() = doTest("""
Hello <b>world</b>.
""", """
Hello world.
""")

    @Test
    @JsName("show_html_inline")
    fun `show html inline`() = doTest("""
Hello <b>world</b>.
""", """
Hello <b>world</b>.
""", showHtml = true)

    @Test
    @JsName("horizontal_rule")
    fun `horizontal rule`() = doTest("""
---
""", """
──────────
""", width = 10)

    @Test
    @JsName("header_1_empty")
    fun `header 1 empty`() = doTest("""
#
""", """


""", width = 19)

    @Test
    @JsName("header_1_custom_padding")
    fun `header 1 custom padding`() = doTest("""
# Header Text
""", """


${h1.colorOnly.colorOnly("═══ ${bold("Header Text")} ═══")}


""", width = 19, theme = Theme {
        dimensions["markdown.header.padding"] = 2
    })

    @Test
    @JsName("header_1")
    fun `header 1`() = doTest("""
# Header Text
""", """

${h1.colorOnly("═══ ${bold("Header Text")} ═══")}

""", width = 19)

    @Test
    @JsName("header_2")
    fun `header 2`() = doTest("""
## Header Text
""", """

${h2.colorOnly("─── ${bold("Header Text")} ───")}

""", width = 19)

    @Test
    @JsName("header_3")
    fun `header 3`() = doTest("""
### Header Text
""", """

${h3.colorOnly("    ${(bold + underline)("Header Text")}    ")}

""", width = 19)

    @Test
    @JsName("header_4")
    fun `header 4`() = doTest("""
#### Header Text
""", """

${h4.colorOnly("    ${underline("Header Text")}    ")}

""", width = 19)

    @Test
    @JsName("header_5")
    fun `header 5`() = doTest("""
##### Header Text
""", """

${h5.colorOnly("    ${italic("Header Text")}    ")}

""", width = 19)

    @Test
    @JsName("header_6")
    fun `header 6`() = doTest("""
###### Header Text
""", """

${h6.colorOnly("    ${dim("Header Text")}    ")}

""", width = 19)

    @Test
    @JsName("header_trailing_chars")
    fun `header trailing chars`() = doTest("""
# Header Text ##
""", """

${h1.colorOnly("═══ ${bold("Header Text")} ═══")}

""", width = 19)

    @Test
    @JsName("setext_h1")
    fun `setext h1`() = doTest("""
Header Text
===========
""", """

${h1.colorOnly("═══ ${bold("Header Text")} ═══")}

""", width = 19)

    @Test
    @JsName("setext_h2")
    fun `setext h2`() = doTest("""
  Header Text  
---
""", """

${h2.colorOnly("─── ${bold("Header Text")} ───")}

""", width = 19)

    @Test
    @JsName("empty_code_span")
    fun `empty code span`() = doTest("""
An `` empty code span.
""", """
An `` empty code span.
""")

    @Test
    @JsName("code_span")
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
    @JsName("hard_line_breaks")
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
    @JsName("header_only_table")
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
    @JsName("simple_table")
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
    @JsName("no_rectangular_table")
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
    @JsName("table_alignment")
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
    @JsName("task_list")
    fun `task list`() = doTest("""
- [ ] foo
- [x] bar
""", """
 • ☐ foo
 • ☑ bar
""")

    @Test
    @JsName("indented_code_block")
    fun `indented code block`() = doTest("""
    foo {
        bar
        
        
        
        baz
    }
""", """
╭───────╮
│${codeBlock("foo {  ")}│
│${codeBlock("    bar")}│
│${codeBlock("       ")}│
│${codeBlock("       ")}│
│${codeBlock("       ")}│
│${codeBlock("    baz")}│
│${codeBlock("}      ")}│
╰───────╯
""")

    @Test
    @JsName("fenced_code_block")
    fun `fenced code block`() = doTest("""
```
foo {
    bar



    baz
}
```
""", """
╭───────╮
│${codeBlock("foo {  ")}│
│${codeBlock("    bar")}│
│${codeBlock("       ")}│
│${codeBlock("       ")}│
│${codeBlock("       ")}│
│${codeBlock("    baz")}│
│${codeBlock("}      ")}│
╰───────╯
""")

    // https://github.github.com/gfm/#example-113
    @Test
    @JsName("fenced_code_block_with_info_string")
    fun `fenced code block with info string`() = doTest("""
~~~~    ruby startline=3 ${'$'}%@#${'$'}
def foo(x)
  return 3
end
~~~~~~~
""", """
╭──────────╮
│${codeBlock("def foo(x)")}│
│${codeBlock("  return 3")}│
│${codeBlock("end       ")}│
╰──────────╯
""")

    @Test
    @JsName("fenced_code_block_with_nbsp")
    fun `fenced code block with nbsp`() {
        val nbsp = '\u00A0'
        doTest("""
```
foo${nbsp}bar baz
```
""", """
╭───────────╮
│${codeBlock("foo${nbsp}bar baz")}│
╰───────────╯
""")
    }

    @Test
    @JsName("fenced_code_block_with_no_border_in_theme")
    fun `fenced code block with no border in theme`() = doTest("""
```
foo  bar
```
""", """
${codeBlock("foo  bar")}
""", theme = Theme {
        flags["markdown.code.block.border"] = false
    })

    @Test
    @JsName("indented_code_block_with_no_border_in_theme")
    fun `indented code block with no border in theme`() = doTest("""
    foo  bar
""", """
${codeBlock("foo  bar")}
""", theme = Theme {
        flags["markdown.code.block.border"] = false
    })

    @Test
    @JsName("plain_theme")
    fun `plain theme`() = doTest("""
# H1
A ***~~span~~*** and `some code`.

[link](c.com)
""", """

═════ H1 ══════

A span and some
code.

link(c.com)
""", theme = Theme.Plain, width = 15)

    @Test
    @JsName("ascii_theme")
    fun `ascii theme`() = doTest("""
# H1
## H2
- [ ] foo
- [x] bar

| a | b |
|---|---|
| c | d |
""", """

=== H1 ===


--- H2 ---

 * [ ] foo
 * [x] bar

+---+---+
| a | b |
+===+===+
| c | d |
+---+---+
""", theme = Theme.PlainAscii, width = 10)

    private fun doTest(
        markdown: String,
        expected: String,
        width: Int = 79,
        showHtml: Boolean = false,
        theme: Theme = Theme.Default,
        hyperlinks: Boolean = false,
    ) {
        val md = markdown.replace("⏎", "")
        try {
            val terminal = Terminal(
                ansiLevel = AnsiLevel.TRUECOLOR,
                width = width,
                theme = theme,
                hyperlinks = hyperlinks
            )
            val actual = terminal.render(Markdown(md, showHtml)).normalizeHyperlinks()
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
