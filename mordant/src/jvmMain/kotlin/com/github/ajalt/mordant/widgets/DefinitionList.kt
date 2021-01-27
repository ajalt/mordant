package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.table.MordantDsl
import com.github.ajalt.mordant.terminal.Terminal

private class DefinitionList(
    private val entries: List<Pair<Widget, Widget>>,
    private val inline: Boolean,
    private val descriptionSpacing: Int,
    private val entrySpacing: Int,
) : Widget {
    init {
        require(descriptionSpacing >= 0) { "Spacing cannot be negative" }
    }

    private val keys get() = entries.map { it.first }
    private val values get() = entries.map { it.second }

    override fun measure(t: Terminal, width: Int): WidthRange {
        val termMeasurement = keys.maxWidthRange(t, width)
        val descMeasurement = measureDescriptions(t, width)
        return termMeasurement + descMeasurement
    }

    private fun measureDescriptions(t: Terminal, width: Int) = values.maxWidthRange(t, width)

    override fun render(t: Terminal, width: Int): Lines {
        if (width == 0) return EMPTY_LINES

        val termMeasurements = keys.map { it.measure(t, width) }
        val maxInlineTermWidth = (width / 2.5).toInt()
        val maxDescWidth = measureDescriptions(t, width).max
        val termWidth: Int = termMeasurements.filter {
            it.max <= maxInlineTermWidth || inline && it.max + descriptionSpacing + maxDescWidth <= width
        }.maxWidthRange { it }.max
        val descOffset = (termWidth + descriptionSpacing).coerceAtLeast(4)
        val lines = mutableListOf<Line>()

        for ((i, entry) in entries.withIndex()) {
            if (i > 0) repeat(entrySpacing) { lines += emptyList<Span>() }
            val (term, desc) = entry
            if (!inline) {
                lines += term.render(t, width).lines
                repeat(descriptionSpacing) { lines += emptyList<Span>() }
                lines += desc.render(t, width).lines
                continue
            }
            if (termMeasurements[i].max > termWidth) {
                lines += term.render(t, width).lines
                lines += desc.withPadding(0, 0, 0, descOffset).render(t, width).lines
                continue
            }
            val termLines = term.render(t, termWidth).lines
            val descLines = desc.render(t, (width - termWidth - descriptionSpacing).coerceAtLeast(0)).lines
            termLines.zip(descLines).mapTo(lines) { (t, d) ->
                flatLine(t, Span.space(descriptionSpacing + termWidth - t.lineWidth), d)
            }

            if (termLines.size > descLines.size) {
                lines += termLines.drop(descLines.size)
            } else if (descLines.size > termLines.size) {
                val paddingLeft = if (descOffset > 0) listOf(Span.space(descOffset)) else EMPTY_LINE
                descLines.drop(termLines.size).mapTo(lines) {
                    if (it.isEmpty()) it else paddingLeft + it
                }
            }
        }

        return Lines(lines)
    }
}

@MordantDsl
class DefinitionListBuilder {
    private val items = mutableListOf<Pair<Widget, Widget>>()

    var inline: Boolean = false

    private var _descriptionSpacing: Int? = null
    private var _entrySpacing: Int? = null

    /**
     * If [inline] is `true`, this is the minimum number of spaces between a term and its
     * description (default 2).
     * If [inline] is `false`, this is the number of blank lines between a term and its
     * description (default 0)
     */
    var descriptionSpacing: Int
        get() = _descriptionSpacing ?: if (inline) 2 else 0
        set(value) {
            _descriptionSpacing = value
        }

    /** The number of blank lines between entries */
    var entrySpacing: Int
        get() = _entrySpacing ?: if (inline) 0 else 1
        set(value) {
            _entrySpacing = value
        }

    fun entry(term: String, description: String) {
        entry(Text(term), Text(description))
    }

    fun entry(term: Widget, description: String) {
        entry(term, Text(description))
    }

    fun entry(term: String, description: Widget) {
        entry(Text(term), description)
    }

    fun entry(term: Widget, description: Widget) {
        items += term to description
    }

    fun entry(init: DefinitionListEntryBuilder.() -> Unit) {
        items += DefinitionListEntryBuilder().apply(init).build()
    }

    internal fun build(): Widget = DefinitionList(items, inline, descriptionSpacing, entrySpacing)
}

@MordantDsl
class DefinitionListEntryBuilder {
    private var term: Widget? = null
    private var desc: Widget? = null
    fun term(term: Widget) {
        this.term = term
    }

    fun term(
        term: String,
        style: TextStyle = DEFAULT_STYLE,
        whitespace: Whitespace = Whitespace.NORMAL,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null
    ) {
        term(Text(term, style, whitespace, align, overflowWrap, width))
    }

    fun description(description: Widget) {
        this.desc = description
    }

    fun description(
        description: String,
        style: TextStyle = DEFAULT_STYLE,
        whitespace: Whitespace = Whitespace.NORMAL,
        align: TextAlign = TextAlign.NONE,
        overflowWrap: OverflowWrap = OverflowWrap.NORMAL,
        width: Int? = null
    ) {
        description(Text(description, style, whitespace, align, overflowWrap, width))
    }

    internal fun build(): Pair<Widget, Widget> = Pair(
        requireNotNull(term) { "Must provide a term" },
        requireNotNull(desc) { "Must provide a description" },
    )
}

fun definitionList(init: DefinitionListBuilder.() -> Unit): Widget {
    return DefinitionListBuilder().apply(init).build()
}
