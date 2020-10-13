package com.github.ajalt.mordant.rendering.markdown

import com.github.ajalt.mordant.Terminal
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.internal.parseText
import com.github.ajalt.mordant.rendering.table.SectionBuilder
import com.github.ajalt.mordant.rendering.table.table
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.parser.MarkdownParser


internal class MarkdownDocument(private val parts: List<Renderable>) : Renderable {
    override fun measure(t: Terminal, width: Int): WidthRange {
        return parts.maxWidthRange(t, width)
    }

    override fun render(t: Terminal, width: Int): Lines {
     return parts.foldLines { it.render(t, width) }
    }
}

private val EOL_LINES = Lines(listOf(EMPTY_LINE, EMPTY_LINE))
private val EOL_TEXT = Text(EOL_LINES, whitespace = Whitespace.PRE)
private val TABLE_DELIMITER_REGEX = Regex(""":?-+:?""")
private val CHECK_BOX_REGEX = Regex("""\[[^]]]""")

private inline fun <T> List<T>.foldLines(transform: (T) -> Lines): Lines {
    return fold(EMPTY_LINES) { l, r -> l + transform(r) }
}

internal class MarkdownRenderer(
        input: String,
        private val theme: Theme,
        private val showHtml: Boolean
) {
    // Hack to work around the fact that the markdown parser doesn't parse CRLF correctly
    private val input = input.replace("\r", "")
    private val linkDestOpen = parseText("(", theme.markdownLinkDestination)
    private val linkDestClose = parseText(")", theme.markdownLinkDestination)

    fun render(): MarkdownDocument {
        val flavour: MarkdownFlavourDescriptor = GFMFlavourDescriptor()
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(input)
        return parseFile(parsedTree)
    }

    private fun parseFile(node: ASTNode): MarkdownDocument {
        require(node.type == MarkdownElementTypes.MARKDOWN_FILE)
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
                val content = Text(lines, whitespace = Whitespace.PRE, style = theme.markdownCodeBlock)
                if (theme.markdownCodeBlockBorder) Panel(content) else content
            }
            MarkdownElementTypes.CODE_BLOCK -> {
                val content = Text(innerInlines(node, drop = 0), whitespace = Whitespace.PRE, style = theme.markdownCodeBlock)
                if (theme.markdownCodeBlockBorder) Panel(content) else content
            }
            MarkdownElementTypes.HTML_BLOCK -> when {
                showHtml -> Text(innerInlines(node, drop = 0), whitespace = Whitespace.PRE)
                else -> Text(EMPTY_LINES)
            }
            MarkdownElementTypes.PARAGRAPH -> {
                Text(innerInlines(node, drop = 0), theme.markdownText)
            }
            MarkdownElementTypes.LINK_DEFINITION -> {
                Text(parseText(nodeText(node), theme.markdownLinkDestination))
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
                val content = CHECK_BOX_REGEX.find(nodeText(node))!!.value.removeSurrounding("[", "]")
                Text(parseText(if (content.isBlank()) "☐ " else "☑ ", DEFAULT_STYLE), whitespace = Whitespace.PRE)
            }

            GFMElementTypes.TABLE -> table {
                parseTableAlignment(node).forEachIndexed { i, align ->
                    column(i) { this.align = align }
                }
                header {
                    style = theme.markdownTableHeader
                    parseTableRow(node.children.first { it.type == GFMElementTypes.HEADER })
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
            MarkdownElementTypes.FULL_REFERENCE_LINK -> {
                innerInlines(node, drop = 0)
            }
            MarkdownElementTypes.LINK_TEXT -> {
                parseText(nodeText(node.children[1]), theme.markdownLinkText)
            }
            MarkdownElementTypes.LINK_LABEL -> {
                parseText(nodeText(node), theme.markdownLinkDestination)
            }
            MarkdownElementTypes.LINK_DESTINATION -> {
                innerInlines(node, drop = if (node.children.firstOrNull()?.type == MarkdownTokenTypes.LT) 1 else 0)
                        .replaceStyle(theme.markdownLinkDestination) // the child might be TEXT or GFM_AUTOLINK
            }
            MarkdownElementTypes.INLINE_LINK -> {
                val text = innerInlines(node.children.first { it.type == MarkdownElementTypes.LINK_TEXT }, drop = 1)
                        .withStyle(theme.markdownLinkText)
                val dest = node.children.find { it.type == MarkdownElementTypes.LINK_DESTINATION }
                        ?.let { parseInlines(it) }
                        ?: EMPTY_LINES
                listOf(text, linkDestOpen, dest, linkDestClose).foldLines { it }
            }
            MarkdownElementTypes.SHORT_REFERENCE_LINK -> {
                innerInlines(node.children[0], drop = 0).withStyle(theme.markdownLinkText)
            }

            MarkdownElementTypes.IMAGE -> {
                // for images, just render the alt text if there is any
                parseInlines(node.children[1])
            }
            // email autolinks are parsed in a plain PARAGRAPH rather than an AUTOLINK, so we'll end
            // up rendering the surrounding <>.
            MarkdownTokenTypes.EMAIL_AUTOLINK,
            GFMTokenTypes.GFM_AUTOLINK,
            MarkdownTokenTypes.AUTOLINK -> parseText(nodeText(node), theme.markdownLinkText)
            MarkdownElementTypes.AUTOLINK -> innerInlines(node, drop = 1)

            // TokenTypes
            MarkdownTokenTypes.BLOCK_QUOTE -> EMPTY_LINES // don't render '>' delimiters in block quotes
            MarkdownTokenTypes.CODE_LINE -> parseText(nodeText(node).drop(4), DEFAULT_STYLE)
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
                parseText(nodeText(node), DEFAULT_STYLE)
            }
            MarkdownTokenTypes.EOL -> {
                // Add an extra linebreak, since the first one will get folded away by foldLines.
                // Parse the text rather than hard coding the return value to support NEL and LS.
                Lines(listOf(EMPTY_LINE) + parseText(nodeText(node), DEFAULT_STYLE).lines)
            }
            else -> error("Unexpected token when parsing inlines: $node; [${node.type}:'${nodeText(node).take(10)}'}]")
        }
    }

    private fun nodeText(node: ASTNode) = input.substring(node.startOffset, node.endOffset)

    private fun innerInlines(node: ASTNode, drop: Int, dropLast: Int = drop): Lines {
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
        val headerSeparator = node.children.first { it.type == GFMTokenTypes.TABLE_SEPARATOR }
        return TABLE_DELIMITER_REGEX.findAll(nodeText(headerSeparator)).map {
            if (it.value.endsWith(":")) {
                if (it.value.startsWith(":")) TextAlign.CENTER
                else TextAlign.RIGHT
            } else TextAlign.LEFT
        }
    }
}
