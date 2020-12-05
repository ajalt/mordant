package com.github.ajalt.mordant.markdown

import com.github.ajalt.mordant.components.*
import com.github.ajalt.mordant.components.NEL
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.BorderStyle.Companion.SQUARE_DOUBLE_SECTION_SEPARATOR
import com.github.ajalt.mordant.internal.generateHyperlinkId
import com.github.ajalt.mordant.internal.parseText
import com.github.ajalt.mordant.table.SectionBuilder
import com.github.ajalt.mordant.table.table
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


internal class MarkdownDocument(private val parts: List<Renderable>) : Renderable {
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

private val EOL_LINES by lazy { Lines(listOf(EMPTY_LINE, EMPTY_LINE)) }
private val EOL_TEXT by lazy { Text(EOL_LINES, whitespace = Whitespace.PRE) }
private val TABLE_DELIMITER_REGEX = Regex(""":?-+:?""")
private val CHECK_BOX_REGEX = Regex("""\[[^]]]""")

private inline fun <T> List<T>.foldLines(transform: (T) -> Lines): Lines {
    return fold(EMPTY_LINES) { l, r -> l + transform(r) }
}

internal class MarkdownRenderer(
        input: String,
        private val theme: Theme,
        private val showHtml: Boolean,
        private val hyperlinks: Boolean
) {
    // Hack to work around the fact that the markdown parser doesn't parse CRLF correctly
    private val input = input.replace("\r", "")

    private var linkMap : LinkMap? = null

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

    private fun parseStructure(node: ASTNode): Renderable {
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
                        .map { parseStructure(it) }))
            }
            MarkdownElementTypes.CODE_FENCE -> {
                val start = node.children.indexOfFirst { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT }
                val end = node.children.indexOfLast { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT }
                val lines = innerInlines(node, drop = start, dropLast = if (end < 0) 0 else node.children.lastIndex - end)
                val content = Text(lines, whitespace = Whitespace.PRE_WRAP, style = theme.markdownCodeBlock)
                if (theme.markdownCodeBlockBorder) Panel(content) else content
            }
            MarkdownElementTypes.CODE_BLOCK -> {
                val content = Text(innerInlines(node), whitespace = Whitespace.PRE_WRAP, style = theme.markdownCodeBlock)
                if (theme.markdownCodeBlockBorder) Panel(content) else content
            }
            MarkdownElementTypes.HTML_BLOCK -> when {
                showHtml -> Text(innerInlines(node), whitespace = Whitespace.PRE_WRAP)
                else -> Text(EMPTY_LINES)
            }
            MarkdownElementTypes.PARAGRAPH -> {
                Text(innerInlines(node), theme.markdownText)
            }
            MarkdownElementTypes.LINK_DEFINITION -> {
                if (hyperlinks) EmptyRenderable
                else Text(parseText(node.nodeText(), theme.markdownLinkDestination))
            }

            MarkdownElementTypes.SETEXT_1 -> setext(theme.markdownH1Rule, theme.markdownH1, node)
            MarkdownElementTypes.SETEXT_2 -> setext(theme.markdownH2Rule, theme.markdownH2, node)
            MarkdownElementTypes.ATX_1 -> atxHorizRule(theme.markdownH1Rule, theme.markdownH1, node)
            MarkdownElementTypes.ATX_2 -> atxHorizRule(theme.markdownH2Rule, theme.markdownH2, node)
            MarkdownElementTypes.ATX_3 -> atxHorizRule(theme.markdownH3Rule, theme.markdownH3, node)
            MarkdownElementTypes.ATX_4 -> atxHorizRule(theme.markdownH4Rule, theme.markdownH4, node)
            MarkdownElementTypes.ATX_5 -> atxHorizRule(theme.markdownH5Rule, theme.markdownH5, node)
            MarkdownElementTypes.ATX_6 -> atxHorizRule(theme.markdownH6Rule, theme.markdownH6, node)

            GFMTokenTypes.CHECK_BOX -> {
                val content = CHECK_BOX_REGEX.find(node.nodeText())!!.value.removeSurrounding("[", "]")
                val checkbox = if (content.isBlank()) theme.markdownTaskUnchecked else theme.markdownTaskChecked
                Text(parseText("$checkbox ", DEFAULT_STYLE), whitespace = Whitespace.PRE)
            }

            GFMElementTypes.TABLE -> table {
                borderStyle = SQUARE_DOUBLE_SECTION_SEPARATOR
                parseTableAlignment(node).forEachIndexed { i, align ->
                    column(i) { this.align = align }
                }
                header {
                    style = theme.markdownTableHeader
                    parseTableRow(node.firstChildOfType(GFMElementTypes.HEADER))
                }
                body {
                    style = theme.markdownTableBody
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
                parseText(input.substring(node.children[1].startOffset,
                        node.children.last().startOffset).trim(), theme.markdownCodeSpan)
            }
            MarkdownElementTypes.EMPH -> {
                innerInlines(node, drop = 1).withStyle(theme.markdownEmph)
            }
            MarkdownElementTypes.STRONG -> {
                innerInlines(node, drop = 2).withStyle(theme.markdownStrong)
            }
            GFMElementTypes.STRIKETHROUGH -> {
                innerInlines(node, drop = 2).withStyle(theme.markdownStikethrough)
            }
            MarkdownElementTypes.LINK_TEXT -> {
                parseText(node.children[1].nodeText(), theme.markdownLinkText)
            }
            MarkdownElementTypes.LINK_LABEL -> {
                parseText(node.nodeText(), theme.markdownLinkDestination)
            }
            MarkdownElementTypes.LINK_DESTINATION -> {
                innerInlines(node, drop = if (node.children.firstOrNull()?.type == MarkdownTokenTypes.LT) 1 else 0)
                        .replaceStyle(theme.markdownLinkDestination) // the child might be TEXT or GFM_AUTOLINK
            }
            MarkdownElementTypes.INLINE_LINK -> {
                parseInlineLink(node)
            }
            MarkdownElementTypes.FULL_REFERENCE_LINK,
            MarkdownElementTypes.SHORT_REFERENCE_LINK -> {
                parseReferenceLink(node, hyperlinks)
            }

            MarkdownElementTypes.IMAGE -> {
                // for images, just render the hyperlink
                parseInlines(node.children[1])
            }
            // email autolinks are parsed in a plain PARAGRAPH rather than an AUTOLINK, so we'll end
            // up rendering the surrounding <>.
            MarkdownTokenTypes.EMAIL_AUTOLINK,
            GFMTokenTypes.GFM_AUTOLINK,
            MarkdownTokenTypes.AUTOLINK -> parseText(node.nodeText(), theme.markdownLinkText)
            MarkdownElementTypes.AUTOLINK -> innerInlines(node, drop = 1)

            MarkdownTokenTypes.HTML_TAG -> when {
                showHtml -> parseText(node.nodeText(), DEFAULT_STYLE)
                else -> EMPTY_LINES
            }
            // TokenTypes
            MarkdownTokenTypes.BLOCK_QUOTE -> EMPTY_LINES // don't render '>' delimiters in block quotes
            MarkdownTokenTypes.CODE_LINE -> parseText(node.nodeText().drop(4), DEFAULT_STYLE)
            MarkdownTokenTypes.HARD_LINE_BREAK -> parseText(NEL, theme.markdownText)
            MarkdownTokenTypes.ESCAPED_BACKTICKS -> parseText("`", theme.markdownText)
            MarkdownTokenTypes.BAD_CHARACTER -> parseText("�", theme.markdownText)
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
            MarkdownTokenTypes.WHITE_SPACE -> {
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

    private fun atxHorizRule(bar: String, style: TextStyle, node: ASTNode): Renderable {
        return when {
            node.children.size <= 1 -> EOL_TEXT
            else -> headerHr(Text(atxContent(node)), bar, style)
        }
    }

    private fun atxContent(node: ASTNode): Lines {
        val (drop, dropLast) = dropWs(node.children[1].children)
        return innerInlines(node.children[1], drop = drop, dropLast = dropLast)
    }

    private fun setext(bar: String, style: TextStyle, node: ASTNode): Renderable {
        val (drop, dropLast) = dropWs(node.children[0].children)
        val content = innerInlines(node.children[0], drop = drop, dropLast = dropLast)
        return headerHr(Text(content), bar, style)
    }

    private fun headerHr(content: Renderable, bar: String, style: TextStyle): Renderable {
        return HorizontalRule(content, bar, titleStyle = style, ruleStyle = TextStyle(style.color, style.bgColor))
                .withVerticalPadding(theme.markdownHeaderPadding)
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
            cell(Text(innerInlines(child, drop = drop, dropLast = dropLast)))
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

    private fun parseInlineLink(node: ASTNode): Lines {
        val text = node.firstChildOfType(MarkdownElementTypes.LINK_TEXT).children[1].nodeText()
        val dest = node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)
                ?.children?.find { it.type == MarkdownTokenTypes.TEXT || it.type == GFMTokenTypes.GFM_AUTOLINK }
                ?.nodeText() ?: ""

        if (hyperlinks && dest.isNotBlank()) {
            return parseText(text, theme.markdownLinkText.copy(hyperlink = dest, hyperlinkId = generateHyperlinkId()))
        }

        val parsedText = parseText(text, theme.markdownLinkText)
        val parsedDest = parseText("($dest)", theme.markdownLinkDestination)
        return listOf(parsedText, parsedDest).foldLines { it }
    }

    private fun parseReferenceLink(node: ASTNode, hyperlinks: Boolean): Lines {
        if (!hyperlinks) return innerInlines(node)

        val text = node.findChildOfType(MarkdownElementTypes.LINK_TEXT)?.children?.get(1)?.nodeText()
        val label = node.firstChildOfType(MarkdownElementTypes.LINK_LABEL).children[1].nodeText()
        return when (val hyperlink = linkMap?.getLinkInfo("[$label]")?.destination?.toString()) {
            null -> innerInlines(node)
            else -> parseText(text ?: label, theme.markdownLinkText.copy(hyperlink = hyperlink, hyperlinkId = generateHyperlinkId()))
        }
    }
}

private fun ASTNode.firstChildOfType(type: IElementType) = findChildOfType(type)!!
