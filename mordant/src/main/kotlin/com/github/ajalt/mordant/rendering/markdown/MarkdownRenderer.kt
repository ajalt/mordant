package com.github.ajalt.mordant.rendering.markdown

import com.github.ajalt.mordant.Terminal
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.internal.parseText
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

private val EMPTY_LINES = Lines(emptyList())
private val EOL_LINES = Lines(listOf(emptyList(), emptyList()))
private val EOL_TEXT = Text(EOL_LINES, whitespace = Whitespace.PRE)

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
                        .map { parseStructure(it.children[1]) }
                )
            }
            MarkdownElementTypes.ORDERED_LIST -> {
                OrderedList(node.children
                        .filter { it.type == MarkdownElementTypes.LIST_ITEM }
                        .map { parseStructure(it.children[1]) }
                )
            }
            MarkdownElementTypes.BLOCK_QUOTE -> {
                BlockQuote(MarkdownDocument(node.children.drop(1)
                        .filter { it.type != MarkdownTokenTypes.WHITE_SPACE }
                        .map { parseStructure(it) }))
            }
            MarkdownElementTypes.CODE_FENCE -> {
                // TODO better start/end linebreak handling
                Text(innerInlines(node, drop = 1), whitespace = Whitespace.PRE, style = theme.markdownCodeBlock)
            }
            MarkdownElementTypes.CODE_BLOCK -> TODO("CODE_BLOCK")
            MarkdownElementTypes.HTML_BLOCK -> when {
                showHtml -> Text(innerInlines(node, drop = 0), whitespace = Whitespace.PRE)
                else -> Text(EMPTY_LINES)
            }
            MarkdownElementTypes.PARAGRAPH -> {
                Text(innerInlines(node, drop = 0), theme.markdownText)
            }
            MarkdownElementTypes.LINK_DEFINITION -> Text("") // ignore these since we don't support links
            MarkdownElementTypes.SETEXT_1 -> setext("═", theme.markdownH1, node, theme)
            MarkdownElementTypes.SETEXT_2 ->  setext("─", theme.markdownH2, node, theme)
            MarkdownElementTypes.ATX_1 -> atxHorizRule("═", theme.markdownH1, node, theme)
            MarkdownElementTypes.ATX_2 -> atxHorizRule("─", theme.markdownH2, node, theme)
            MarkdownElementTypes.ATX_3 -> atxHorizRule(" ", theme.markdownH3, node, theme)
            MarkdownElementTypes.ATX_4 -> atxText(theme.markdownH4, node, theme)
            MarkdownElementTypes.ATX_5 -> atxText(theme.markdownH5, node, theme)
            MarkdownElementTypes.ATX_6 -> atxText(theme.markdownH6, node, theme)

            GFMElementTypes.STRIKETHROUGH -> TODO("STRIKETHROUGH")
            GFMElementTypes.TABLE -> TODO("TABLE")
            GFMElementTypes.HEADER -> TODO("HEADER")
            GFMElementTypes.ROW -> TODO("ROW")

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

            // GFMTokenTypes
            GFMTokenTypes.TILDE -> TODO("TILDE")
            GFMTokenTypes.TABLE_SEPARATOR -> TODO("TABLE_SEPARATOR")
            GFMTokenTypes.CHECK_BOX -> TODO("CHECK_BOX")
            GFMTokenTypes.CELL -> TODO("CELL")

            // TokenTypes
            MarkdownTokenTypes.CODE_LINE -> TODO("CODE_LINE")
            MarkdownTokenTypes.SINGLE_QUOTE -> TODO("SINGLE_QUOTE")
            MarkdownTokenTypes.DOUBLE_QUOTE -> TODO("DOUBLE_QUOTE")
            MarkdownTokenTypes.HARD_LINE_BREAK -> parseText(NEL, theme.markdownText)
            MarkdownTokenTypes.LINK_ID -> TODO("LINK_ID")
            MarkdownTokenTypes.ATX_HEADER -> TODO("ATX_HEADER")
            MarkdownTokenTypes.ATX_CONTENT -> TODO("ATX_CONTENT")
            MarkdownTokenTypes.SETEXT_CONTENT -> TODO("SETEXT_CONTENT")
            MarkdownTokenTypes.ESCAPED_BACKTICKS -> parseText("`", theme.markdownText)
            MarkdownTokenTypes.FENCE_LANG -> TODO("FENCE_LANG")
            MarkdownTokenTypes.BAD_CHARACTER -> parseText("�", theme.markdownText)
            MarkdownTokenTypes.AUTOLINK,
            MarkdownTokenTypes.EMAIL_AUTOLINK, // email autolinks are parsed in a plain PARAGRAPH rather than an AUTOLINK, so we'll end up rendering the surrounding <>.
            MarkdownTokenTypes.TEXT,
            MarkdownTokenTypes.HTML_BLOCK_CONTENT,
            MarkdownTokenTypes.LPAREN,
            MarkdownTokenTypes.RPAREN,
            MarkdownTokenTypes.LBRACKET,
            MarkdownTokenTypes.RBRACKET,
            MarkdownTokenTypes.LT,
            MarkdownTokenTypes.GT,
            MarkdownTokenTypes.COLON,
            MarkdownTokenTypes.EXCLAMATION_MARK,
            MarkdownTokenTypes.EMPH,
            MarkdownTokenTypes.BACKTICK,
            MarkdownTokenTypes.CODE_FENCE_CONTENT,
            MarkdownTokenTypes.URL,
            MarkdownTokenTypes.WHITE_SPACE,
            GFMTokenTypes.GFM_AUTOLINK -> {
                parseText(input.substring(node.startOffset, node.endOffset), DEFAULT_STYLE)
            }
            MarkdownTokenTypes.EOL -> EOL_LINES
            else -> error("Unexpected token when parsing inlines: $node")
        }
    }

    private fun innerInlines(node: ASTNode, drop: Int, dropLast: Int = drop): Lines {
            return node.children.subList(drop, node.children.size - dropLast)
                    .foldLines { parseInlines(it) }
    }

    private fun atxHorizRule(bar: String, style: TextStyle, node: ASTNode, theme: Theme): Renderable {
        return when {
            node.children.size <= 1 -> EOL_TEXT
            else -> Padded(HorizontalRule(bar, atxContent(node).lines[0], style), Padding.vertical(theme.markdownHeaderPadding))
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
        // TODO: multiline headers
        return Padded(HorizontalRule(bar, content.lines[0], style), Padding.vertical(theme.markdownHeaderPadding))
    }

    private fun dropWs(nodes: List<ASTNode>): Pair<Int, Int> {
        val drop = if (nodes.firstOrNull()?.type == MarkdownTokenTypes.WHITE_SPACE) 1 else 0
        val dropLast = if (nodes.lastOrNull()?.type == MarkdownTokenTypes.WHITE_SPACE) 1 else 0
        return drop to dropLast
    }
}
