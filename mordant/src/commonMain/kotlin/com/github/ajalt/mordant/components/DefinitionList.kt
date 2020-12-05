package com.github.ajalt.mordant.components

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal

class DefinitionList(
        private val items: Map<out Renderable, Renderable>,
        private val inline: Boolean = false,
        private val inlineSpacing: Int = 2
) : Renderable {
    constructor(
            vararg items: Pair<String, String>,
            inline: Boolean = false,
            inlineSpacing: Int = 2
    ) : this(items.associate { Text(it.first) to Text(it.second) }, inline, inlineSpacing)

    override fun measure(t: Terminal, width: Int): WidthRange {
        val termMeasurement = items.keys.maxWidthRange(t, width)
        val descMeasurement = measureDescriptions(t, width)
        return termMeasurement + descMeasurement
    }

    private fun measureDescriptions(t: Terminal, width: Int) = items.values.maxWidthRange(t, width)

    override fun render(t: Terminal, width: Int): Lines {
        if (width == 0) return EMPTY_LINES

        val termMeasurements = items.keys.map { it.measure(t, width) }
        val maxInlineTermWidth = (width / 2.5).toInt()
        val maxDescWidth = measureDescriptions(t, width).max
        val termWidth: Int = termMeasurements.filter {
            it.max <= maxInlineTermWidth || inline && it.max + inlineSpacing + maxDescWidth <= width
        }.maxWidthRange { it }.max
        val descOffset = (termWidth + inlineSpacing).coerceAtLeast(4)
        val lines = mutableListOf<Line>()

        for ((i, entry) in items.entries.withIndex()) {
            val (term, desc) = entry
            if (!inline || termMeasurements[i].max > termWidth) {
                lines += term.render(t, width).lines
                lines += Padded.get(desc, Padding(left = descOffset)).render(t, width).lines
                continue
            }

            val termLines = term.render(t, termWidth).lines
            val descLines = desc.render(t, width - termWidth - inlineSpacing).lines
            termLines.zip(descLines).mapTo(lines) { (t, d) ->
                flatLine(t, Span.space(inlineSpacing + termWidth - t.lineWidth), d)
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
