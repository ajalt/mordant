package com.github.ajalt.mordant.markdown

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.BorderType.Companion.ASCII_DOUBLE_SECTION_SEPARATOR
import com.github.ajalt.mordant.rendering.BorderType.Companion.SQUARE_DOUBLE_SECTION_SEPARATOR
import com.github.ajalt.mordant.table.SectionBuilder
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.table.verticalLayout
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

private const val NEL = "\u0085"

@Suppress("PrivatePropertyName")
internal class MarkdownRenderer(
    input: String,
    private val theme: Theme,
    private val showHtml: Boolean,
    private val hyperlinks: Boolean,
) {
    private val EOL_TEXT = Text("\n", whitespace = Whitespace.PRE)
    private val EMPTY_TEXT = Text("")
    private val TABLE_DELIMITER_REGEX = Regex(""":?-+:?""")

    @Suppress("RegExpRedundantEscape") // extra escape required on js
    private val CHECK_BOX_REGEX = Regex("""\[[^\]]\]""")

    // Hack to work around the fact that the markdown parser doesn't parse CRLF correctly
    private val input = input.replace("\r", "")

    private var linkMap: LinkMap? = null

    fun render(): Widget {
        val flavour: MarkdownFlavourDescriptor = GFMFlavourDescriptor()
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(input)
        return parseFile(parsedTree)
    }

    private fun parseFile(node: ASTNode): Widget {
        require(node.type == MarkdownElementTypes.MARKDOWN_FILE)
        if (hyperlinks) linkMap = LinkMap.buildLinkMap(node, input)
        return parseBlocks(node.children)
    }

    private fun parseBlocks(nodes: List<ASTNode>): Widget {
        return verticalLayout {
            for ((i, node) in nodes.withIndex()) {
                // skip the extra EOL after a PARAGRAPH, since the layout adds it for us
                if (node.type == MarkdownTokenTypes.EOL
                    && i != 0
                    && i != nodes.lastIndex
                    && nodes[i - 1].type == MarkdownElementTypes.PARAGRAPH) {
                    continue
                }
                if (node.type == MarkdownTokenTypes.WHITE_SPACE) {
                    continue
                }
                cell(parseStructure(node))
            }
        }
    }

    private fun parseStructure(node: ASTNode): Widget {
        return when (node.type) {
            // ElementTypes
            MarkdownElementTypes.UNORDERED_LIST -> {
                UnorderedList(node.children
                    .filter { it.type == MarkdownElementTypes.LIST_ITEM }
                    .map { parseBlocks(it.children.drop(1)) }
                )
            }

            MarkdownElementTypes.ORDERED_LIST -> {
                OrderedList(node.children
                    .filter { it.type == MarkdownElementTypes.LIST_ITEM }
                    .map { parseBlocks(it.children.drop(1)) }
                )
            }

            MarkdownElementTypes.BLOCK_QUOTE -> {
                BlockQuote(
                    parseBlocks(
                        node.children.drop(1).filter { it.type != MarkdownTokenTypes.WHITE_SPACE }
                    )
                )
            }

            MarkdownElementTypes.CODE_FENCE -> {
                val start =
                    node.children.indexOfFirst { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT }
                val end =
                    node.children.indexOfLast { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT }
                val dropLast = if (end < 0) 0 else node.children.lastIndex - end
                val inner = innerInlines(node, drop = start, dropLast = dropLast)
                val content = Text(
                    theme.style("markdown.code.block")(inner),
                    whitespace = Whitespace.PRE_WRAP
                )
                if (theme.flag("markdown.code.block.border")) Panel(content) else content
            }

            MarkdownElementTypes.CODE_BLOCK -> {
                val content = Text(
                    theme.style("markdown.code.block")(innerInlines(node)),
                    whitespace = Whitespace.PRE_WRAP
                )
                if (theme.flag("markdown.code.block.border")) Panel(content) else content
            }

            MarkdownElementTypes.HTML_BLOCK -> when {
                showHtml -> Text(innerInlines(node), whitespace = Whitespace.PRE_WRAP)
                else -> Text("")
            }

            MarkdownElementTypes.PARAGRAPH -> {
                Text(innerInlines(node), whitespace = Whitespace.NORMAL)
            }

            MarkdownElementTypes.LINK_DEFINITION -> {
                if (hyperlinks) EmptyWidget
                else Text(
                    theme.style("markdown.link.destination")(node.nodeText()),
                    whitespace = Whitespace.NORMAL
                )
            }

            MarkdownElementTypes.SETEXT_1 -> setext(
                theme.string("markdown.h1.rule"),
                theme.style("markdown.h1"),
                node
            )

            MarkdownElementTypes.SETEXT_2 -> setext(
                theme.string("markdown.h2.rule"),
                theme.style("markdown.h2"),
                node
            )

            MarkdownElementTypes.ATX_1 -> atxHr(
                theme.string("markdown.h1.rule"),
                theme.style("markdown.h1"),
                node
            )

            MarkdownElementTypes.ATX_2 -> atxHr(
                theme.string("markdown.h2.rule"),
                theme.style("markdown.h2"),
                node
            )

            MarkdownElementTypes.ATX_3 -> atxHr(
                theme.string("markdown.h3.rule"),
                theme.style("markdown.h3"),
                node
            )

            MarkdownElementTypes.ATX_4 -> atxHr(
                theme.string("markdown.h4.rule"),
                theme.style("markdown.h4"),
                node
            )

            MarkdownElementTypes.ATX_5 -> atxHr(
                theme.string("markdown.h5.rule"),
                theme.style("markdown.h5"),
                node
            )

            MarkdownElementTypes.ATX_6 -> atxHr(
                theme.string("markdown.h6.rule"),
                theme.style("markdown.h6"),
                node
            )

            GFMTokenTypes.CHECK_BOX -> {
                val content =
                    CHECK_BOX_REGEX.find(node.nodeText())!!.value.removeSurrounding("[", "]")
                val checkbox = when {
                    content.isBlank() -> theme.string("markdown.task.unchecked")
                    else -> theme.string("markdown.task.checked")
                }
                Text("$checkbox ", whitespace = Whitespace.PRE)
            }

            GFMElementTypes.TABLE -> table {
                borderType = when {
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

            MarkdownTokenTypes.HORIZONTAL_RULE -> HorizontalRule()
            MarkdownTokenTypes.EOL -> EMPTY_TEXT
            MarkdownTokenTypes.WHITE_SPACE -> EMPTY_TEXT
            else -> error("Unexpected token when parsing structure: $node")
        }
    }

    private fun parseInlines(node: ASTNode): String {
        return when (node.type) {
            // ElementTypes
            MarkdownElementTypes.CODE_SPAN -> {
                // Trim the code as a kludge to prevent the background style from extending if the
                // code ends with whitespace. It would be better to fix this in the text wrapping code.
                theme.style("markdown.code.span")(
                    input.substring(
                        node.children[1].startOffset,
                        node.children.last().startOffset
                    ).trim()
                )
            }

            MarkdownElementTypes.EMPH -> {
                theme.style("markdown.emph")(innerInlines(node, drop = 1))
            }

            MarkdownElementTypes.STRONG -> {
                theme.style("markdown.strong")(innerInlines(node, drop = 2))
            }

            GFMElementTypes.STRIKETHROUGH -> {
                theme.style("markdown.stikethrough")(innerInlines(node, drop = 2))
            }

            MarkdownElementTypes.LINK_TEXT -> {
                theme.style("markdown.link.text")(node.children[1].nodeText())
            }

            MarkdownElementTypes.LINK_LABEL -> {
                theme.style("markdown.link.destination")(node.nodeText())
            }

            MarkdownElementTypes.LINK_DESTINATION -> {
                // the child might be TEXT or GFM_AUTOLINK
                val drop = if (node.children.firstOrNull()?.type == MarkdownTokenTypes.LT) 1 else 0
                theme.style("markdown.link.destination")(innerInlines(node, drop = drop))
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
            -> theme.style("markdown.link.text")(node.nodeText())

            MarkdownElementTypes.AUTOLINK -> innerInlines(node, drop = 1)
            MarkdownTokenTypes.HTML_TAG -> if (showHtml) node.nodeText() else ""

            // TokenTypes
            MarkdownTokenTypes.BLOCK_QUOTE -> "" // don't render '>' delimiters in block quotes
            MarkdownTokenTypes.CODE_LINE -> node.nodeText().drop(4)
            MarkdownTokenTypes.HARD_LINE_BREAK -> NEL
            MarkdownTokenTypes.ESCAPED_BACKTICKS -> "`"
            MarkdownTokenTypes.BAD_CHARACTER -> "�"
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
            GFMTokenTypes.TILDE,
            -> node.nodeText()

            MarkdownTokenTypes.EOL -> {
                // Return the text rather than hard coding the return value to support NEL and LS.
                node.nodeText()
            }

            else -> error(
                "Unexpected token when parsing inlines: $node; [${node.type}:'${
                    node.nodeText().take(10)
                }'}]"
            )
        }
    }

    private fun ASTNode.nodeText(drop: Int = 0) =
        input.substring(startOffset + drop, endOffset - drop)

    private fun innerInlines(node: ASTNode, drop: Int = 0, dropLast: Int = drop): String {
        return node.children.subList(drop, node.children.size - dropLast)
            .joinToString("") { parseInlines(it) }
    }

    private fun atxHr(bar: String, style: TextStyle, node: ASTNode): Widget {
        return when {
            node.children.size <= 1 -> EOL_TEXT
            else -> headerHr(
                Text(
                    style(atxContent(node)),
                    whitespace = Whitespace.NORMAL
                ), bar, style
            )
        }
    }

    private fun atxContent(node: ASTNode): String {
        val (drop, dropLast) = dropWs(node.children[1].children)
        return innerInlines(node.children[1], drop = drop, dropLast = dropLast)
    }

    private fun setext(bar: String, style: TextStyle, node: ASTNode): Widget {
        val (drop, dropLast) = dropWs(node.children[0].children)
        val content = innerInlines(node.children[0], drop = drop, dropLast = dropLast)
        return headerHr(Text(style(content), whitespace = Whitespace.NORMAL), bar, style)
    }

    private fun headerHr(content: Widget, bar: String, style: TextStyle): Widget {
        return HorizontalRule(
            content,
            ruleCharacter = bar,
            ruleStyle = TextStyle(style.color, style.bgColor)
        )
            .withPadding { vertical = theme.dimension("markdown.header.padding") }
    }

    private fun dropWs(nodes: List<ASTNode>): Pair<Int, Int> {
        val drop = if (nodes.firstOrNull()?.type == MarkdownTokenTypes.WHITE_SPACE) 1 else 0
        val dropLast =
            if (nodes.size > 1 && nodes.last().type == MarkdownTokenTypes.WHITE_SPACE) 1 else 0
        return drop to dropLast
    }

    private fun SectionBuilder.parseTableRow(node: ASTNode) = row {
        for (child in node.children) {
            if (child.type != GFMTokenTypes.CELL) continue
            val (drop, dropLast) = dropWs(child.children)
            cell(
                Text(
                    innerInlines(child, drop = drop, dropLast = dropLast),
                    whitespace = Whitespace.NORMAL
                )
            )
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

    private fun renderInlineLink(node: ASTNode): String {
        val text = findLinkText(node)!!
        val dest = findLinkDest(node) ?: ""
        if (hyperlinks && dest.isNotBlank()) {
            val style = (theme.style("markdown.link.text") + TextStyle(hyperlink = dest))
            return style(text)
        }

        val parsedText = theme.style("markdown.link.text")(text)
        val parsedDest = theme.style("markdown.link.destination")("($dest)")
        return return "$parsedText $parsedDest"
    }

    private fun renderReferenceLink(node: ASTNode): String {
        TODO()
//        if (!hyperlinks) return innerInlines(node)
//
//        val label = findLinkLabel(node)!!
//        return when (val hyperlink = linkMap?.getLinkInfo("[$label]")?.destination?.toString()) {
//            null -> innerInlines(node)
//            else -> {
//                val style = theme.style("markdown.link.text")
//                    .copy(hyperlink = hyperlink, hyperlinkId = generateHyperlinkId())
//                findLinkText(node)?.replaceStyle(style) ?: parseText(label, style)
//            }
//        }
    }

    private fun renderImageLink(node: ASTNode): String {
        val link = node.findChildOfType(MarkdownElementTypes.INLINE_LINK)
            ?: node.findChildOfType(MarkdownElementTypes.FULL_REFERENCE_LINK)
            ?: node.findChildOfType(MarkdownElementTypes.SHORT_REFERENCE_LINK)
            ?: return ""
        val text = findLinkText(link)
            ?.takeUnless { it.isEmpty() }
            ?: return ""
        return "🖼️ ${theme.style("markdown.img.alt-text")(text)}"
    }

    private fun findLinkLabel(node: ASTNode): String? {
        return node.findChildOfType(MarkdownElementTypes.LINK_LABEL)?.children?.get(1)?.nodeText()
    }

    private fun findLinkDest(node: ASTNode): String? {
        return node.findChildOfType(MarkdownElementTypes.LINK_DESTINATION)
            ?.children?.find { it.type == MarkdownTokenTypes.TEXT || it.type == GFMTokenTypes.GFM_AUTOLINK }
            ?.nodeText()
    }

    private fun findLinkText(node: ASTNode): String? {
        return node.findChildOfType(MarkdownElementTypes.LINK_TEXT)
            ?.let { innerInlines(it, drop = 1) }
    }
}

private fun ASTNode.firstChildOfType(type: IElementType) = findChildOfType(type)!!
