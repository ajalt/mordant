package com.github.ajalt.mordant.widgets

import com.github.ajalt.mordant.internal.EMPTY_LINE
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal

data class Padding(val top: Int, val right: Int, val bottom: Int, val left: Int) {
    constructor(all: Int) : this(all, all, all, all)

    companion object {
        inline operator fun invoke(block: Builder.() -> Unit): Padding {
            val b = Builder(0, 0, 0, 0).apply(block)
            return Padding(b.top, b.right, b.bottom, b.left)
        }
    }

    init {
        require(top >= 0) { "Invalid negative top padding" }
        require(right >= 0) { "Invalid negative right padding" }
        require(bottom >= 0) { "Invalid negative bottom padding" }
        require(left >= 0) { "Invalid negative left padding" }
    }

    val isEmpty get() = top == 0 && right == 0 && bottom == 0 && left == 0

    data class Builder(var top: Int, var right: Int, var bottom: Int, var left: Int) {
        var horizontal: Int
            get() = maxOf(left, right)
            set(value) {
                left = value
                right = value
            }

        var vertical: Int
            get() = maxOf(top, bottom)
            set(value) {
                top = value
                bottom = value
            }

        var all: Int
            get() = maxOf(top, bottom, left, right)
            set(value) {
                top = value
                bottom = value
            }
    }
}

/**
 * Add padding around this widget.
 *
 * By default, horizontal padding will be added to every line of the output, even if the line is empty. If you set
 * [padEmptyLines] to `false`, no padding will be added to empty lines.
 */
fun Widget.withPadding(padding: Padding, padEmptyLines: Boolean = true): Widget =
    Padded.get(this, padding, padEmptyLines)

fun Widget.withPadding(all: Int, padEmptyLines: Boolean = true): Widget =
    Padded.get(this, Padding(all), padEmptyLines)

fun Widget.withPadding(top: Int, right: Int, bottom: Int, left: Int, padEmptyLines: Boolean = true): Widget =
    Padded.get(this, Padding(top, right, bottom, left), padEmptyLines)

fun Widget.withPadding(padEmptyLines: Boolean = true, padding: Padding.Builder.() -> Unit): Widget =
    Padded.get(this, Padding(padding), padEmptyLines)

operator fun Padding.plus(other: Padding): Padding {
    return Padding(
        top = top + other.top,
        right = right + other.right,
        bottom = bottom + other.bottom,
        left = left + other.left
    )
}

internal class Padded private constructor(
    internal val content: Widget,
    private val padding: Padding,
    private val padEmptyLines: Boolean,
) : Widget {
    companion object {
        fun get(content: Widget, padding: Padding, padEmptyLines: Boolean): Widget {
            return when {
                padding.isEmpty -> content
                content is Padded -> Padded(content.content, content.padding + padding, padEmptyLines)
                else -> Padded(content, padding, padEmptyLines)
            }
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
