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
                if (node.children.any { it.type == MarkdownElementTypes.CODE_SPAN }) {
                    paragraphWithCodeSpan(node, theme)
                } else {
                    Text(innerInlines(node, drop = 0), theme.markdownText)
                }
            }
            MarkdownElementTypes.LINK_DEFINITION -> Text("") // ignore these since we don't support links
            MarkdownElementTypes.SETEXT_1 -> TODO("SETEXT_1")
            MarkdownElementTypes.SETEXT_2 -> TODO("SETEXT_2")
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
            MarkdownElementTypes.SETEXT_1 -> TODO("SETEXT_1")
            MarkdownElementTypes.SETEXT_2 -> TODO("SETEXT_2")

            // GFMTokenTypes
            GFMTokenTypes.TILDE -> TODO("TILDE")
            GFMTokenTypes.TABLE_SEPARATOR -> TODO("TABLE_SEPARATOR")
            GFMTokenTypes.CHECK_BOX -> TODO("CHECK_BOX")
            GFMTokenTypes.CELL -> TODO("CELL")

            // TokenTypes
            MarkdownTokenTypes.CODE_LINE -> TODO("CODE_LINE")
            MarkdownTokenTypes.BLOCK_QUOTE -> TODO("BLOCK_QUOTE")
            MarkdownTokenTypes.SINGLE_QUOTE -> TODO("SINGLE_QUOTE")
            MarkdownTokenTypes.DOUBLE_QUOTE -> TODO("DOUBLE_QUOTE")
            MarkdownTokenTypes.HARD_LINE_BREAK -> TODO("HARD_LINE_BREAK")
            MarkdownTokenTypes.LINK_ID -> TODO("LINK_ID")
            MarkdownTokenTypes.ATX_HEADER -> TODO("ATX_HEADER")
            MarkdownTokenTypes.ATX_CONTENT -> TODO("ATX_CONTENT")
            MarkdownTokenTypes.SETEXT_1 -> TODO("SETEXT_1")
            MarkdownTokenTypes.SETEXT_2 -> TODO("SETEXT_2")
            MarkdownTokenTypes.SETEXT_CONTENT -> TODO("SETEXT_CONTENT")
            MarkdownTokenTypes.ESCAPED_BACKTICKS -> TODO("ESCAPED_BACKTICKS")
            MarkdownTokenTypes.LIST_NUMBER -> TODO("LIST_NUMBER")
            MarkdownTokenTypes.FENCE_LANG -> TODO("FENCE_LANG")
            MarkdownTokenTypes.CODE_FENCE_START -> TODO("CODE_FENCE_START")
            MarkdownTokenTypes.CODE_FENCE_END -> TODO("CODE_FENCE_END")
            MarkdownTokenTypes.LINK_TITLE -> TODO("LINK_TITLE")
            MarkdownTokenTypes.HTML_TAG -> TODO("HTML_TAG")
            MarkdownTokenTypes.BAD_CHARACTER -> parseText("�", DEFAULT_STYLE)
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
        val drop = when (node.children[1].children.firstOrNull()?.type) {
            MarkdownTokenTypes.WHITE_SPACE -> 1
            else -> 0
        }
        return innerInlines(node.children[1], drop = drop, dropLast = 0)
    }

    // Since code spans are inline elements that affect wrapping, we generate several text elements
    // and concat them.
    private fun paragraphWithCodeSpan(paragraph: ASTNode, theme: Theme): MarkdownDocument {
        val parts = mutableListOf<Text>()
        var start = 0
        var i = 0
        while (i <= paragraph.children.lastIndex) {
            val node = paragraph.children[i]
            if (node.type != MarkdownElementTypes.CODE_SPAN) {
                i += 1
                continue
            }
            if (i > start) {
                parts += Text(paragraph.children.subList(start, i).foldLines { parseInlines(it) }, theme.markdownText)
            }
            val text = input.substring(node.children[1].startOffset, node.children.last().startOffset)
            val parsed = parseText(text, this.theme.markdownCodeSpan)

            // Since normal wrapping will trim leading whitespace, we need to manually add a
            // preformatted space to the end of code spans.
            val lines = when (paragraph.children.getOrNull(i + 1)?.type) {
                MarkdownTokenTypes.WHITE_SPACE, MarkdownTokenTypes.EOL -> {
                    Lines(listOf(parsed.lines.single() + Span.word(" ", theme.markdownText)))
                }
                else -> parsed
            }
            parts += Text(lines, whitespace = Whitespace.PRE)
            start = i + 1
            i += 1
        }

        if (i > start) {
            parts += Text(paragraph.children.subList(start, i).foldLines { parseInlines(it) }, style = theme.markdownText)
        }

        return MarkdownDocument(parts)
    }
}
