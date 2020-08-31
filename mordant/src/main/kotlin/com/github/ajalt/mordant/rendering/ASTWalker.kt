package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.TermColors
import com.github.ajalt.mordant.Terminal
import vendor.org.intellij.markdown.MarkdownElementTypes
import vendor.org.intellij.markdown.MarkdownTokenTypes
import vendor.org.intellij.markdown.ast.ASTNode
import vendor.org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import vendor.org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import vendor.org.intellij.markdown.parser.MarkdownParser


fun main() {
    val src = """
    For *example*, to **always** output ANSI RGB color codes, even if stdout is currently directed to a file,
    you can do this:
    
    ```
    pre {
        *format*
    }
    ```
    """.trimIndent()
    val flavour: MarkdownFlavourDescriptor = GFMFlavourDescriptor()
    val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(src)
    println(parsedTree.children.joinToString("\n"))
    println(ASTWalker(src).render())
}

private class ASTWalker(
        private val input: String,
        private val terminal: Terminal = Terminal(TermColors(TermColors.Level.TRUECOLOR))
) {
    fun render(): String {
        val flavour: MarkdownFlavourDescriptor = GFMFlavourDescriptor()
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(input)
        val renderables = parseFile(parsedTree)
        return renderables.joinToString("") { terminal.render(it) }
    }

    private fun parseFile(node: ASTNode): List<Renderable> {
        require(node.type == MarkdownElementTypes.MARKDOWN_FILE)
        return node.children.map { parseStructure(it) }
    }

    private fun parseStructure(node: ASTNode): Renderable {
        return when (node.type) {
            // ElementTypes
            MarkdownElementTypes.UNORDERED_LIST -> TODO("UNORDERED_LIST")
            MarkdownElementTypes.ORDERED_LIST -> TODO("ORDERED_LIST")
            MarkdownElementTypes.LIST_ITEM -> TODO("LIST_ITEM")
            MarkdownElementTypes.BLOCK_QUOTE -> TODO("BLOCK_QUOTE")
            MarkdownElementTypes.CODE_FENCE -> {
                var spans = innerInlines(node)
                if (spans.firstOrNull()?.text == "\n") spans = spans.drop(1)
                if (spans.lastOrNull()?.text == "\n") spans = spans.dropLast(1)
                Text(spans, whitespace = Whitespace.PRE)
            }
            MarkdownElementTypes.CODE_BLOCK -> TODO("CODE_BLOCK")
            MarkdownElementTypes.CODE_SPAN -> TODO("CODE_SPAN")
            MarkdownElementTypes.HTML_BLOCK -> TODO("HTML_BLOCK")
            MarkdownElementTypes.PARAGRAPH -> {
                Text(node.children.flatMap { parseInlines(it) })
            }
            MarkdownElementTypes.LINK_DEFINITION -> TODO("LINK_DEFINITION")
            MarkdownElementTypes.LINK_LABEL -> TODO("LINK_LABEL")
            MarkdownElementTypes.LINK_DESTINATION -> TODO("LINK_DESTINATION")
            MarkdownElementTypes.LINK_TITLE -> TODO("LINK_TITLE")
            MarkdownElementTypes.LINK_TEXT -> TODO("LINK_TEXT")
            MarkdownElementTypes.INLINE_LINK -> TODO("INLINE_LINK")
            MarkdownElementTypes.FULL_REFERENCE_LINK -> TODO("FULL_REFERENCE_LINK")
            MarkdownElementTypes.SHORT_REFERENCE_LINK -> TODO("SHORT_REFERENCE_LINK")
            MarkdownElementTypes.IMAGE -> TODO("IMAGE")
            MarkdownElementTypes.AUTOLINK -> TODO("AUTOLINK")
            MarkdownElementTypes.SETEXT_1 -> TODO("SETEXT_1")
            MarkdownElementTypes.SETEXT_2 -> TODO("SETEXT_2")
            MarkdownElementTypes.ATX_1 -> TODO("ATX_1")
            MarkdownElementTypes.ATX_2 -> TODO("ATX_2")
            MarkdownElementTypes.ATX_3 -> TODO("ATX_3")
            MarkdownElementTypes.ATX_4 -> TODO("ATX_4")
            MarkdownElementTypes.ATX_5 -> TODO("ATX_5")
            MarkdownElementTypes.ATX_6 -> TODO("ATX_6")
            MarkdownTokenTypes.EOL -> {
                Text(listOf(Span.line()))
            }
            else -> error("Unexpected token when parsing structure: $node")
        }
    }

    private fun parseInlines(node: ASTNode): List<Span> {
        return when (node.type) {
            // ElementTypes
            MarkdownElementTypes.UNORDERED_LIST -> TODO("UNORDERED_LIST")
            MarkdownElementTypes.ORDERED_LIST -> TODO("ORDERED_LIST")
            MarkdownElementTypes.LIST_ITEM -> TODO("LIST_ITEM")
            MarkdownElementTypes.BLOCK_QUOTE -> TODO("BLOCK_QUOTE")
            MarkdownElementTypes.CODE_BLOCK -> TODO("CODE_BLOCK")
            MarkdownElementTypes.CODE_SPAN -> TODO("CODE_SPAN")
            MarkdownElementTypes.HTML_BLOCK -> TODO("HTML_BLOCK")
            MarkdownElementTypes.EMPH -> {
                innerInlines(node).map { it.withStyle(TextStyle.ITALIC) }
            }
            MarkdownElementTypes.STRONG -> {
                innerInlines(node, drop = 2).map { it.withStyle(TextStyle.BOLD) }
            }
            MarkdownElementTypes.LINK_DEFINITION -> TODO("LINK_DEFINITION")
            MarkdownElementTypes.LINK_LABEL -> TODO("LINK_LABEL")
            MarkdownElementTypes.LINK_DESTINATION -> TODO("LINK_DESTINATION")
            MarkdownElementTypes.LINK_TITLE -> TODO("LINK_TITLE")
            MarkdownElementTypes.LINK_TEXT -> TODO("LINK_TEXT")
            MarkdownElementTypes.INLINE_LINK -> TODO("INLINE_LINK")
            MarkdownElementTypes.FULL_REFERENCE_LINK -> TODO("FULL_REFERENCE_LINK")
            MarkdownElementTypes.SHORT_REFERENCE_LINK -> TODO("SHORT_REFERENCE_LINK")
            MarkdownElementTypes.IMAGE -> TODO("IMAGE")
            MarkdownElementTypes.AUTOLINK -> TODO("AUTOLINK")
            MarkdownElementTypes.SETEXT_1 -> TODO("SETEXT_1")
            MarkdownElementTypes.SETEXT_2 -> TODO("SETEXT_2")
            MarkdownElementTypes.ATX_1 -> TODO("ATX_1")
            MarkdownElementTypes.ATX_2 -> TODO("ATX_2")
            MarkdownElementTypes.ATX_3 -> TODO("ATX_3")
            MarkdownElementTypes.ATX_4 -> TODO("ATX_4")
            MarkdownElementTypes.ATX_5 -> TODO("ATX_5")
            MarkdownElementTypes.ATX_6 -> TODO("ATX_6")

            // TokenTypes
            MarkdownTokenTypes.CODE_LINE -> TODO("CODE_LINE")
            MarkdownTokenTypes.BLOCK_QUOTE -> TODO("BLOCK_QUOTE")
            MarkdownTokenTypes.HTML_BLOCK_CONTENT -> TODO("HTML_BLOCK_CONTENT")
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
            MarkdownTokenTypes.LIST_BULLET -> TODO("LIST_BULLET")
            MarkdownTokenTypes.URL -> TODO("URL")
            MarkdownTokenTypes.HORIZONTAL_RULE -> TODO("HORIZONTAL_RULE")
            MarkdownTokenTypes.LIST_NUMBER -> TODO("LIST_NUMBER")
            MarkdownTokenTypes.FENCE_LANG -> TODO("FENCE_LANG")
            MarkdownTokenTypes.CODE_FENCE_START -> TODO("CODE_FENCE_START")
            MarkdownTokenTypes.CODE_FENCE_END -> TODO("CODE_FENCE_END")
            MarkdownTokenTypes.LINK_TITLE -> TODO("LINK_TITLE")
            MarkdownTokenTypes.AUTOLINK -> TODO("AUTOLINK")
            MarkdownTokenTypes.EMAIL_AUTOLINK -> TODO("EMAIL_AUTOLINK")
            MarkdownTokenTypes.HTML_TAG -> TODO("HTML_TAG")
            MarkdownTokenTypes.BAD_CHARACTER -> TODO("BAD_CHARACTER")
            MarkdownTokenTypes.TEXT,
            MarkdownTokenTypes.EOL,
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
            MarkdownTokenTypes.WHITE_SPACE -> {
                listOf(Span(input.substring(node.startOffset, node.endOffset)))
            }
            else -> error("Unexpected token when parsing inlines: $node")
        }
    }

    private fun innerInlines(node: ASTNode, drop: Int = 1): List<Span> {
        return node.children.drop(drop).dropLast(drop).flatMap { parseInlines(it) }
    }
}
