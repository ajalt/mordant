package com.github.ajalt.mordant.markdown

import com.github.ajalt.mordant.internal.DEFAULT_STYLE
import com.github.ajalt.mordant.internal.EMPTY_LINE
import com.github.ajalt.mordant.internal.EMPTY_LINES
import com.github.ajalt.mordant.internal.generateHyperlinkId
import com.github.ajalt.mordant.internal.parseText
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.BorderStyle.Companion.ASCII_DOUBLE_SECTION_SEPARATOR
import com.github.ajalt.mordant.rendering.BorderStyle.Companion.SQUARE_DOUBLE_SECTION_SEPARATOR
import com.github.ajalt.mordant.table.SectionBuilder
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.*
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkdownParser


internal class MarkdownDocument(private val parts: List<Widget>) : Widget {
    override fun measure(t: Terminal, width: Int): WidthRange {
        // This isn't measure isn't correct in some cases, e.g. task lists that place renderables on
        // the same line. We use a RenderableBuilder instead of this class to get accurate
        // measurements, but it makes parsing md blocks more complicated. The worst case scenario is
        // that a table column is a little narrow, truncating the markdown.
        return parts.maxWidthRange(t, width)
    }

    override fun render(t: Terminal, width: Int): Lines {
        return parts.foldLines { it.render(t, width) }
    }
}

private inline fun <T> List<T>.foldLines(transform: (T) -> Lines): Lines {
    return fold(EMPTY_LINES) { l, r ->
        val other = transform(r)
        when {
            l.lines.isEmpty() -> other
            other.lines.isEmpty() -> l
            else -> Lines(
                listOf(
                    l.lines.dropLast(1),
                    listOf(Line(l.lines.last() + other.lines.first())),
                    other.lines.drop(1)
                ).flatten()
            )
        }
    }
}

@Suppress("PrivatePropertyName")
internal class MarkdownRenderer(
    input: String,
    private val theme: Theme,
    private val showHtml: Boolean,
    private val hyperlinks: Boolean,
) {
    private val EOL_LINES = Lines(listOf(EMPTY_LINE, EMPTY_LINE))
    private val EOL_TEXT = Text(EOL_LINES, whitespace = Whitespace.PRE)
    private val TABLE_DELIMITER_REGEX = Regex(""":?-+:?""")

    @Suppress("RegExpRedundantEscape") // extra escape required on js
    private val CHECK_BOX_REGEX = Regex("""\[[^\]]]""")

    // Hack to work around the fact that the markdown parser doesn't parse CRLF correctly
    private val input = input.replace("\r", "")

    private var linkMap: LinkMap? = null

    fun render(): MarkdownDocument {
        val flavour: MarkdownFlavourDescriptor = GFMFlavourDescriptor()
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(input)
        return parseFile(parsedTree)
    }

    private fun parseFile(node: ASTNode): MarkdownDocument {
        require(node.type == MarkdownElementTypes.MARKDOWN_FILE)
        if (hyperlinks) linkMap = LinkMap.buildLinkMap(node, input)
        return MarkdownDocument(node.children.map { parseStructure(it) })
    }

    private fun parseStructure(node: ASTNode): Widget {
        return when (node.type) {
            // ElementTypes
            MarkdownElementTypes.UNORDERED_LIST -> {
                UnorderedList(node.children
                    .filter { it.type == MarkdownElementTypes.LIST_ITEM }
                    .map { MarkdownDocument(it.children.drop(1).map { c -> parseStructure(c) }) }
                )
            }
            MarkdownElementTypes.ORDERED_LIST -> {
                OrderedList(node.children
                    .filter { it.type == MarkdownElementTypes.LIST_ITEM }
                    .map { MarkdownDocument(it.children.drop(1).map { c -> parseStructure(c) }) }
                )
            }
            MarkdownElementTypes.BLOCK_QUOTE -> {
                BlockQuote(MarkdownDocument(node.children.drop(1)
                    .filter { it.type != MarkdownTokenTypes.WHITE_SPACE }
                    .map { parseStructure(it) })
                )
            }
            MarkdownElementTypes.CODE_FENCE -> {
                val start = node.children.indexOfFirst { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT }
                val end = node.children.indexOfLast { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT }
                val dropLast = if (end < 0) 0 else node.children.lastIndex - end
                val lines = innerInlines(node, drop = start, dropLast = dropLast)
                val content = Text(
                    lines.withStyle(theme.style("markdown.code.block")),
                    whitespace = Whitespace.PRE_WRAP
                )
                if (theme.flag("markdown.code.block.border")) Panel(content) else content
            }
            MarkdownElementTypes.CODE_BLOCK -> {
                val content = Text(
                    innerInlines(node).withStyle(theme.style("markdown.code.block")),
                    whitespace = Whitespace.PRE_WRAP
                )
                if (theme.flag("markdown.code.block.border")) Panel(content) else content
            }
            MarkdownElementTypes.HTML_BLOCK -> when {
                showHtml -> Text(innerInlines(node), whitespace = Whitespace.PRE_WRAP)
                else -> Text(EMPTY_LINES)
            }
            MarkdownElementTypes.PARAGRAPH -> {
                Text(innerInlines(node), whitespace = Whitespace.NORMAL)
            }
            MarkdownElementTypes.LINK_DEFINITION -> {
                if (hyperlinks) EmptyWidget
                else Text(parseText(node.nodeText(), theme.style("markdown.link.destination")), whitespace = Whitespace.NORMAL)
            }

            MarkdownElementTypes.SETEXT_1 -> setext(theme.string("markdown.h1.rule"), theme.style("markdown.h1"), node)
            MarkdownElementTypes.SETEXT_2 -> setext(theme.string("markdown.h2.rule"), theme.style("markdown.h2"), node)
            MarkdownElementTypes.ATX_1 -> atxHr(theme.string("markdown.h1.rule"), theme.style("markdown.h1"), node)
            MarkdownElementTypes.ATX_2 -> atxHr(theme.string("markdown.h2.rule"), theme.style("markdown.h2"), node)
            MarkdownElementTypes.ATX_3 -> atxHr(theme.string("markdown.h3.rule"), theme.style("markdown.h3"), node)
            MarkdownElementTypes.ATX_4 -> atxHr(theme.string("markdown.h4.rule"), theme.style("markdown.h4"), node)
            MarkdownElementTypes.ATX_5 -> atxHr(theme.string("markdown.h5.rule"), theme.style("markdown.h5"), node)
            MarkdownElementTypes.ATX_6 -> atxHr(theme.string("markdown.h6.rule"), theme.style("markdown.h6"), node)

            GFMTokenTypes.CHECK_BOX -> {
                val content = CHECK_BOX_REGEX.find(node.nodeText())!!.value.removeSurrounding("[", "]")
                val checkbox = when {
                    content.isBlank() -> theme.string("markdown.task.unchecked")
                    else -> theme.string("markdown.task.checked")
                }
                Text(parseText("$checkbox ", DEFAULT_STYLE), whitespace = Whitespace.PRE)
            }

            GFMElementTypes.TABLE -> table {
                borderStyle = when {
                    theme.flag("markdown.table.ascii") -> ASCII_DOUBLE_SECTION_SEPARATOR
                    else -> SQUARE_DOUBLE_SECTION_SEPARATOR
                }
                parseTableAlignment(node).forEachIndexed { i, align ->
                    column(i) { this.align = align }
                }
                header {
                    style = theme.style("markdown.table.header")
                    parseTableRow(node.firstChildOfType(GFMElementTypes.HEADER))
                }
                body {
                    style = theme.style("markdown.table.body")
                    node.children.filter { it.type == GFMElementTypes.ROW }.forEach {
                        parseTableRow(it)
                    }
                }
            }

            MarkdownTokenTypes.HORIZONTAL_RULE -> HorizontalRule(title = "")
            MarkdownTokenTypes.EOL -> EOL_TEXT
            MarkdownTokenTypes.WHITE_SPACE -> Text(EMPTY_LINES)
            else -> error("Unexpected token when parsing structure: $node")
        }
    }

    private fun parseInlines(node: ASTNode): Lines {
        return when (node.type) {
            // ElementTypes
            MarkdownElementTypes.CODE_SPAN -> {
                // Trim the code as a kludge to prevent the background style from extending if the
                // code ends with whitespace. It would be better to fix this in the text wrapping code.
                parseText(
                    input.substring(
                        node.children[1].startOffset,
                        node.children.last().startOffset
                    ).trim(), theme.style("markdown.code.span")
                )
            }
            MarkdownElementTypes.EMPH -> {
                innerInlines(node, drop = 1).withStyle(theme.style("markdown.emph"))
            }
            MarkdownElementTypes.STRONG -> {
                innerInlines(node, drop = 2).withStyle(theme.style("markdown.strong"))
            }
            GFMElementTypes.STRIKETHROUGH -> {
                innerInlines(node, drop = 2).withStyle(theme.style("markdown.stikethrough"))
            }
            MarkdownElementTypes.LINK_TEXT -> {
                parseText(node.children[1].nodeText(), theme.style("markdown.link.text"))
            }
            MarkdownElementTypes.LINK_LABEL -> {
                parseText(node.nodeText(), theme.style("markdown.link.destination"))
            }
            MarkdownElementTypes.LINK_DESTINATION -> {
                innerInlines(node, drop = if (node.children.firstOrNull()?.type == MarkdownTokenTypes.LT) 1 else 0)
                    .replaceStyle(theme.style("markdown.link.destination")) // the child might be TEXT or GFM_AUTOLINK
            }
            MarkdownElementTypes.INLINE_LINK -> {
                renderInlineLink(node)
            }
            MarkdownElementTypes.FULL_REFERENCE_LINK,
            MarkdownElementTypes.SHORT_REFERENCE_LINK,
            -> {
                renderReferenceLink(node)
            }
            MarkdownElementTypes.IMAGE -> {
                renderImageLink(node)
            }
            // email autolinks are parsed in a plain PARAGRAPH rather than an AUTOLINK, so we'll end
            // up rendering the surrounding <>.
            MarkdownTokenTypes.EMAIL_AUTOLINK,
            GFMTokenTypes.GFM_AUTOLINK,
            MarkdownTokenTypes.AUTOLINK,
            -> parseText(node.nodeText(), theme.style("markdown.link.text"))
            MarkdownElementTypes.AUTOLINK -> innerInlines(node, drop = 1)

            MarkdownTokenTypes.HTML_TAG -> when {
                showHtml -> parseText(node.nodeText(), DEFAULT_STYLE)
                else -> EMPTY_LINES
            }
            // TokenTypes
            MarkdownTokenTypes.BLOCK_QUOTE -> EMPTY_LINES // don't render '>' delimiters in block quotes
            MarkdownTokenTypes.CODE_LINE -> parseText(node.nodeText().drop(4), DEFAULT_STYLE)
            MarkdownTokenTypes.HARD_LINE_BREAK -> parseText(NEL, DEFAULT_STYLE)
            MarkdownTokenTypes.ESCAPED_BACKTICKS -> parseText("`", DEFAULT_STYLE)
            MarkdownTokenTypes.BAD_CHARACTER -> parseText("�", DEFAULT_STYLE)
            MarkdownTokenTypes.BACKTICK,
            MarkdownTokenTypes.CODE_FENCE_CONTENT,
            MarkdownTokenTypes.COLON,
            MarkdownTokenTypes.DOUBLE_QUOTE,
            MarkdownTokenTypes.EMPH,
            MarkdownTokenTypes.EXCLAMATION_MARK,
            MarkdownTokenTypes.GT,
            MarkdownTokenTypes.HTML_BLOCK_CONTENT,
            MarkdownTokenTypes.LBRACKET,
            MarkdownTokenTypes.LPAREN,
            MarkdownTokenTypes.LT,
            MarkdownTokenTypes.RBRACKET,
            MarkdownTokenTypes.RPAREN,
            MarkdownTokenTypes.SINGLE_QUOTE,
            MarkdownTokenTypes.TEXT,
            MarkdownTokenTypes.URL,
            MarkdownTokenTypes.WHITE_SPACE,
            -> {
                parseText(node.nodeText(), DEFAULT_STYLE)
            }
            MarkdownTokenTypes.EOL -> {
                // Add an extra linebreak, since the first one will get folded away by foldLines.
                // Parse the text rather than hard coding the return value to support NEL and LS.
                Lines(listOf(EMPTY_LINE) + parseText(node.nodeText(), DEFAULT_STYLE).lines)
            }
            else -> error("Unexpected token when parsing inlines: $node; [${node.type}:'${node.nodeText().take(10)}'}]")
        }
    }

    private fun ASTNode.nodeText(drop: Int = 0) = input.substring(startOffset + drop, endOffset - drop)

    private fun innerInlines(node: ASTNode, drop: Int = 0, dropLast: Int = drop): Lines {
        return node.children.subList(drop, node.children.size - dropLast)
            .foldLines { parseInlines(it) }
    }

    private fun atxHr(bar: String, style: TextStyle, node: ASTNode): Widget {
        return when {
            node.children.size <= 1 -> EOL_TEXT
            else -> headerHr(Text(atxContent(node).withStyle(style), whitespace = Whitespace.NORMAL), bar, style)
        }
    }

    private fun atxContent(node: ASTNode): Lines {
        val (drop, dropLast) = dropWs(node.children[1].children)
        return innerInlines(node.children[1], drop = drop, dropLast = dropLast)
    }

    private fun setext(bar: String, style: TextStyle, node: ASTNode): Widget {
        val (drop, dropLast) = dropWs(node.children[0].children)
        val content = innerInlines(node.children[0], drop = drop, dropLast = dropLast)
        return headerHr(Text(content.withStyle(style), whitespace = Whitespace.NORMAL), bar, style)
    }

    private fun headerHr(content: Widget, bar: String, style: TextStyle): Widget {
        return HorizontalRule(content, ruleCharacter = bar, ruleStyle = TextStyle(style.color, style.bgColor))
            .withVerticalPadding(theme.dimension("markdown.header.padding"))
    }

    private fun dropWs(nodes: List<ASTNode>): Pair<Int, Int> {
        val drop = if (nodes.firstOrNull()?.type == MarkdownTokenTypes.WHITE_SPACE) 1 else 0
        val dropLast = if (nodes.lastOrNull()?.type == MarkdownTokenTypes.WHITE_SPACE) 1 else 0
        return drop to dropLast
    }

    private fun SectionBuilder.parseTableRow(node: ASTNode) = row {
        for (child in node.children) {
            if (child.type != GFMTokenTypes.CELL) continue
            val (drop, dropLast) = dropWs(child.children)
            cell(Text(innerInlines(child, drop = drop, dropLast = dropLast), whitespace = Whitespace.NORMAL))
        }
    }

    private fun parseTableAlignment(node: ASTNode): Sequence<TextAlign> {
        val headerSeparator = node.firstChildOfType(GFMTokenTypes.TABLE_SEPARATOR)
        return TABLE_DELIMITER_REGEX.findAll(headerSeparator.nodeText()).map {
            if (it.value.endsWith(":")) {
                if (it.value.startsWith(":")) TextAlign.CENTER
                else TextAlign.RIGHT
            } else TextAlign.LEFT
        }
    }

    private fun renderInlineLink(node: ASTNode): Lines {
        val text = findLinkText(node)!!
        val dest = findLinkDest(node) ?: ""
        if (hyperlinks && dest.isNotBlank()) {
            return text.replaceStyle(
                theme.style("markdown.link.text").copy(hyperlink = dest, hyperlinkId = generateHyperlinkId())
            )
        }

        val parsedText = text.replaceStyle(theme.style("markdown.link.text"))
        val parsedDest = parseText("($dest)", theme.style("markdown.link.destination"))
        return listOf(parsedText, parsedDest).foldLines { it }
    }

    private fun renderReferenceLink(node: ASTNode): Lines {
        if (!hyperlinks) return innerInlines(node)

        val label = findLinkLabel(node)!!
        return when (val hyperlink = linkMap?.getLinkInfo("[$label]")?.destination?.toString()) {
            null -> innerInlines(node)
            else -> {
                val style = theme.style("markdown.link.text")
                    .copy(hyperlink = hyperlink, hyperlinkId = generateHyperlinkId())
                findLinkText(node)?.replaceStyle(style) ?: parseText(label, style)
            }
        }
    }

    private fun renderImageLink(node: ASTNode): Lines {
        val text = findLinkText(node.firstChildOfType(MarkdownElementTypes.INLINE_LINK))
            ?.takeUnless { it.isEmpty() }
            ?: return EMPTY_LINES
        return listOf(
            parseText("🖼️ ", DEFAULT_STYLE),
            text.replaceStyle(theme.style("markdown.img.alt-text"))
        ).foldLines { it }
    }

    private fun findLinkLabel(node: ASTNode): String? {
        return node.findChildOfType(MarkdownElementTypes.LINK_LABEL)?.children?.get(1)?.nodeText()
    }

    private fun findLinkDest(node: ASTNode): String? {
        return node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)
            ?.children?.find { it.type == MarkdownTokenTypes.TEXT || it.type == GFMTokenTypes.GFM_AUTOLINK }
            ?.nodeText()
    }

    private fun findLinkText(node: ASTNode): Lines? {
        return node.findChildOfType(MarkdownElementTypes.LINK_TEXT)
            ?.let { innerInlines(it, drop = 1) }
    }
}

private fun ASTNode.firstChildOfType(type: IElementType) = findChildOfType(type)!!
