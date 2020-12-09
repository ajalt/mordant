package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.terminal.Terminal

class ProgressBar(
    private var total: Int = 100,
    private var completed: Int = 0,
    private val width: Int? = null,
    private val pendingChar: String? = null,
    private val separatorChar: String? = null,
    private val completeChar: String? = null,
    private val pendingStyle: TextStyle? = null,
    private val separatorStyle: TextStyle? = null,
    private val completeStyle: TextStyle? = null,
    private val finishedStyle: TextStyle? = null,
) : Renderable {
    fun update(completed: Int) {
        this.completed = completed
    }

    fun update(completed: Int, total: Int) {
        this.completed = completed
        this.total = total
    }

    val percentComplete: Float get() = (completed.toFloat() / total).coerceIn(0f, 1f)

    // Cache output for performance since these will usually be rendered many times
    private var lastRender = Triple(-1, -1, EMPTY_LINES)

    private fun width(renderWidth: Int) = width ?: renderWidth

    override fun measure(t: Terminal, width: Int): WidthRange {
        return WidthRange(width(width), width(width))
    }

    override fun render(t: Terminal, width: Int): Lines {
        val w = width(width)
        val completedLength = (percentComplete * w).toInt()

        if (w == lastRender.first && completedLength == lastRender.second) {
            return lastRender.third
        }

        val cc = completeChar ?: t.theme.string("progressbar.complete")
        val pc = pendingChar ?: t.theme.string("progressbar.pending")
        val sc = separatorChar ?: t.theme.string("progressbar.separator")

        val cs = completeStyle ?: t.theme.style("progressbar.complete")
        val ps = pendingStyle ?: t.theme.style("progressbar.pending")
        val ss = separatorStyle ?: t.theme.style("progressbar.separator")
        val fs = finishedStyle ?: t.theme.style("progressbar.finished")

        if (completedLength == w) {
            return makeLines(w, completedLength, segmentText(cc, w, fs))
        }

        val sep = when {
            completedLength > 0 -> segmentText(sc, 1, ss)
            else -> null
        }
        val sepLength = sep?.cellWidth ?: 0
        val complete = segmentText(cc, completedLength, cs)
        val pending = segmentText(pc, w - completedLength - sepLength, ps)

        return makeLines(w, completedLength, complete, sep, pending)
    }

    private fun makeLines(w: Int, completedLength: Int, vararg spans: Span?): Lines {
        return Lines(listOf(listOfNotNull(*spans))).also {
            lastRender = Triple(w, completedLength, it)
        }
    }

    private fun segmentText(char: String, count: Int, style: TextStyle): Span? {
        val text = char.repeat(count)
        return if (text.isEmpty()) null else Span.word(text, style)
    }
}
