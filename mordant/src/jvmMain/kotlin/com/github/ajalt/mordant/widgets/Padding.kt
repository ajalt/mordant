package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal

data class Padding(val top: Int, val right: Int, val bottom: Int, val left: Int) {
    constructor(all: Int) : this(all, all, all, all)
    constructor(vertical: Int, horizontal: Int) : this(vertical, horizontal, vertical, horizontal)
    constructor(top: Int, horizontal: Int, bottom: Int) : this(top, horizontal, bottom, horizontal)

    companion object {
        fun none(): Padding = Padding(0)
        fun vertical(padding: Int = 0): Padding = Padding(padding, 0)
        fun horizontal(padding: Int = 0): Padding = Padding(0, padding)
        fun of(top: Int = 0, right: Int = 0, bottom: Int = 0, left: Int = 0): Padding =
            Padding(top, right, bottom, left)
    }

    init {
        require(top >= 0) { "Invalid negative top padding" }
        require(right >= 0) { "Invalid negative right padding" }
        require(bottom >= 0) { "Invalid negative bottom padding" }
        require(left >= 0) { "Invalid negative left padding" }
    }

    val isEmpty get() = top == 0 && right == 0 && bottom == 0 && left == 0
}

fun Widget.withPadding(padding: Padding, padEmptyLines: Boolean = true): Widget =
    Padded.get(this, padding, padEmptyLines)

fun Widget.withPadding(all: Int, padEmptyLines: Boolean = true): Widget =
    Padded.get(this, Padding(all), padEmptyLines)

fun Widget.withPadding(vertical: Int, horizontal: Int, padEmptyLines: Boolean = true): Widget =
    Padded.get(this, Padding(vertical, horizontal), padEmptyLines)

fun Widget.withPadding(top: Int, horizontal: Int, bottom: Int, padEmptyLines: Boolean = true): Widget =
    Padded.get(this, Padding(top, horizontal, bottom), padEmptyLines)

fun Widget.withPadding(top: Int, right: Int, bottom: Int, left: Int, padEmptyLines: Boolean = true): Widget =
    Padded.get(this, Padding(top, right, bottom, left), padEmptyLines)

fun Widget.withVerticalPadding(padding: Int, padEmptyLines: Boolean = true): Widget =
    withPadding(Padding.vertical(padding), padEmptyLines)

fun Widget.withHorizontalPadding(padding: Int, padEmptyLines: Boolean = true): Widget =
    withPadding(Padding.horizontal(padding), padEmptyLines)

private class Padded private constructor(
    private val content: Widget,
    private val padding: Padding,
    private val padEmptyLines: Boolean,
) : Widget {
    companion object {
        fun get(content: Widget, padding: Padding, padEmptyLines: Boolean): Widget {
            return if (padding.isEmpty) content else Padded(content, padding, padEmptyLines)
        }
    }

    private val paddingWidth get() = padding.left + padding.right

    override fun measure(t: Terminal, width: Int): WidthRange {
        return content.measure(t, width - paddingWidth) + paddingWidth
    }

    override fun render(t: Terminal, width: Int): Lines {
        val lines = content.render(t, (width - paddingWidth).coerceAtLeast(0))

        val output = ArrayList<Line>(padding.top + lines.height + padding.bottom)
        val left = if (padding.left > 0) listOf(Span.space(padding.left)) else EMPTY_LINE
        val right = if (padding.right > 0) listOf(Span.space(padding.right)) else EMPTY_LINE

        repeat(padding.top) { output.add(EMPTY_LINE) }

        for (line in lines.lines) {
            output += when {
                !padEmptyLines && line.isEmpty() -> EMPTY_LINE
                else -> flatLine(left, line, right)
            }
        }

        repeat(padding.bottom) { output.add(EMPTY_LINE) }

        return Lines(output)
    }
}
