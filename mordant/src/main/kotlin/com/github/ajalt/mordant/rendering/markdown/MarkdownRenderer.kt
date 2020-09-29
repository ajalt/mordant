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

private val EOL_LINES = Lines(listOf(emptyList(), emptyList()))
private val EOL_TEXT = Text(EOL_LINES, whitespace = Whitespace.PRE)
private val TABLE_DELIMITER_REGEX = Regex(""":?-+:?""")
private val CHECK_BOX_REGEX = Regex("""\[[^]]]""")

private inline fun <T> List<T>.foldLines(transform: (T) -> Lines): Lines {
    return fold(EMPTY_LINES) { l, r -> l + transform(r) }
}

internal class MarkdownRenderer(
        private val input: String,
        private val theme: Theme,
        private val showHtml: Boolean
) {
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
                Panel(Text(lines, whitespace = Whitespace.PRE, style = theme.markdownCodeBlock))
            }
            MarkdownElementTypes.CODE_BLOCK -> {
                Panel(Text(innerInlines(node, drop = 0), whitespace = Whitespace.PRE, style = theme.markdownCodeBlock))
            }
            MarkdownElementTypes.HTML_BLOCK -> when {
                showHtml -> Text(innerInlines(node, drop = 0), whitespace = Whitespace.PRE)
                else -> Text(EMPTY_LINES)
            }
            MarkdownElementTypes.PARAGRAPH -> {
                Text(innerInlines(node, drop = 0), theme.markdownText)
            }
            MarkdownElementTypes.LINK_DEFINITION -> Text("") // ignore these since we don't support links
            MarkdownElementTypes.SETEXT_1 -> setext("═", theme.markdownH1, node, theme)
            MarkdownElementTypes.SETEXT_2 -> setext("─", theme.markdownH2, node, theme)
            MarkdownElementTypes.ATX_1 -> atxHorizRule("═", theme.markdownH1, node, theme)
            MarkdownElementTypes.ATX_2 -> atxHorizRule("─", theme.markdownH2, node, theme)
            MarkdownElementTypes.ATX_3 -> atxHorizRule(" ", theme.markdownH3, node, theme)
            MarkdownElementTypes.ATX_4 -> atxText(theme.markdownH4, node, theme)
            MarkdownElementTypes.ATX_5 -> atxText(theme.markdownH5, node, theme)
            MarkdownElementTypes.ATX_6 -> atxText(theme.markdownH6, node, theme)

            GFMTokenTypes.CHECK_BOX -> {
                val content = CHECK_BOX_REGEX.find(nodeText(node))!!.value.removeSurrounding("[", "]")
                Text(parseText(if (content.isBlank()) "☐ " else "☑ ", DEFAULT_STYLE), whitespace = Whitespace.PRE)
            }

            GFMElementTypes.TABLE -> table {
                parseTableAlignment(node).forEachIndexed { i, align ->
                    column(i) { this.align = align }
                }
                header {
                    parseTableRow(node.children.first { it.type == GFMElementTypes.HEADER })
                }
                body {
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
                parseText(input.substring(node.children[1].startOffset,
                        node.children.last().startOffset), theme.markdownCodeSpan)
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
            MarkdownElementTypes.INLINE_LINK,
            MarkdownElementTypes.FULL_REFERENCE_LINK,
            MarkdownElementTypes.SHORT_REFERENCE_LINK -> {
                // first child is LINK_TEXT, second is LINK_LABEL. Ignore the label and brackets.
                innerInlines(node.children[0], drop = 1)
            }
            MarkdownElementTypes.IMAGE -> {
                // for images, just render the alt text if there is any
                parseInlines(node.children[1])
            }
            MarkdownElementTypes.AUTOLINK -> innerInlines(node, drop = 1)

            // TokenTypes
            MarkdownTokenTypes.CODE_LINE -> parseText(nodeText(node).drop(4), DEFAULT_STYLE)
            MarkdownTokenTypes.HARD_LINE_BREAK -> parseText(NEL, theme.markdownText)
            MarkdownTokenTypes.ESCAPED_BACKTICKS -> parseText("`", theme.markdownText)
            MarkdownTokenTypes.BAD_CHARACTER -> parseText("�", theme.markdownText)
            MarkdownTokenTypes.AUTOLINK,
            MarkdownTokenTypes.BACKTICK,
            MarkdownTokenTypes.CODE_FENCE_CONTENT,
            MarkdownTokenTypes.COLON,
            MarkdownTokenTypes.DOUBLE_QUOTE,
            MarkdownTokenTypes.EMAIL_AUTOLINK, // email autolinks are parsed in a plain PARAGRAPH rather than an AUTOLINK, so we'll end up rendering the surrounding <>.
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
            GFMTokenTypes.GFM_AUTOLINK -> {
                parseText(nodeText(node), DEFAULT_STYLE)
            }
            MarkdownTokenTypes.EOL -> EOL_LINES
            else -> error("Unexpected token when parsing inlines: $node")
        }
    }

    private fun nodeText(node: ASTNode) = input.substring(node.startOffset, node.endOffset)

    private fun innerInlines(node: ASTNode, drop: Int, dropLast: Int = drop): Lines {
        return node.children.subList(drop, node.children.size - dropLast)
                .foldLines { parseInlines(it) }
    }

    private fun atxHorizRule(bar: String, style: TextStyle, node: ASTNode, theme: Theme): Renderable {
        return when {
            node.children.size <= 1 -> EOL_TEXT
            else -> Padded(HorizontalRule(Text(atxContent(node)), bar, style), Padding.vertical(theme.markdownHeaderPadding))
        }
    }

    private fun atxText(style: TextStyle, node: ASTNode, theme: Theme): Renderable {
        return Padded(Text(when {
            node.children.size <= 1 -> EOL_LINES
            else -> atxContent(node)
        }, style = style), Padding.vertical(theme.markdownHeaderPadding))
    }

    private fun atxContent(node: ASTNode): Lines {
        val (drop, dropLast) = dropWs(node.children[1].children)
        return innerInlines(node.children[1], drop = drop, dropLast = dropLast)
    }

    private fun setext(bar: String, style: TextStyle, node: ASTNode, theme: Theme): Renderable {
        val (drop, dropLast) = dropWs(node.children[0].children)
        val content = innerInlines(node.children[0], drop = drop, dropLast = dropLast)
        return Padded(HorizontalRule(Text(content), bar, style), Padding.vertical(theme.markdownHeaderPadding))
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

