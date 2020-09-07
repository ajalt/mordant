package com.github.ajalt.mordant.rendering

import com.github.ajalt.mordant.Terminal

data class Padding(val top: Int = 0, val right: Int = 0, val bottom: Int = 0, val left: Int = 0) {
    init {
        require(top >= 0) { "Invalid negative top padding" }
        require(right >= 0) { "Invalid negative right padding" }
        require(bottom >= 0) { "Invalid negative bottom padding" }
        require(left >= 0) { "Invalid negative left padding" }
    }

    companion object {
        fun all(padding: Int): Padding = Padding(padding, padding, padding, padding)
        fun symmetrical(vertical: Int = 0, horizontal: Int = 0): Padding = Padding(vertical, horizontal, vertical, horizontal)
        fun vertical(padding: Int = 0): Padding = Padding(padding, 0, padding, 0)
        fun horizontal(padding: Int = 0): Padding = Padding(0, padding, 0, padding)
    }

    val isEmpty = top == 0 && right == 0 && bottom == 0 && left == 0
}

internal class Padded(private val content: Renderable, private val padding: Padding) : Renderable {
    companion object {
        fun get(content: Renderable, padding: Padding) = if (padding.isEmpty) content else Padded(content, padding)
    }

    override fun render(t: Terminal, width: Int): Lines {
        val lines = content.render(t, width)

        val blank = emptyList<Span>()
        val output = ArrayList<Line>(padding.top + lines.lines.size + padding.bottom)
        val left = if (padding.left > 0) listOf(Span.word(" ".repeat(padding.left))) else emptyList()
        val right = if (padding.right > 0) listOf(Span.word(" ".repeat(padding.right))) else emptyList()

        repeat(padding.top) { output.add(blank) }

        for (line in lines.lines) {
            output += listOf(left, line, right).flatten()
        }

        repeat(padding.bottom) { output.add(blank) }

        return Lines(output)
    }

    override fun measure(t: Terminal, width: Int): WidthRange {
        val paddingWidth = padding.left + padding.right
        return content.measure(t, width - paddingWidth) + paddingWidth
    }
}
