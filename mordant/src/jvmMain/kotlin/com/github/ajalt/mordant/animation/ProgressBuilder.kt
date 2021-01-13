package com.github.ajalt.mordant.animation

import com.github.ajalt.mordant.components.Text
import com.github.ajalt.mordant.rendering.DEFAULT_STYLE
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.terminal.Terminal

// TODO: themes
class ProgressBuilder internal constructor() {
    var frameRate: Int = 10
    var historyLength: Float = 30f
    var autoUpdate: Boolean = true
    var padding: Int = 2

    internal var timeSource: () -> Long = { System.nanoTime() }

    fun text(text: String, style: TextStyle = DEFAULT_STYLE) {
        cells += TextProgressCell(Text(text, style))
    }

    fun percentage(style: TextStyle = DEFAULT_STYLE) {
        cells += PercentageProgressCell(style)
    }

    fun progressBar(width: Int? = null) {
        cells += BarProgressCell(width)
    }

    fun completed(suffix: String = "", includeTotal: Boolean = true, style: TextStyle = DEFAULT_STYLE) {
        cells += CompletedProgressCell(suffix, includeTotal, style)
    }

    fun speed(suffix: String = "it/s", style: TextStyle = DEFAULT_STYLE, frameRate: Int? = 1) {
        cells += SpeedProgressCell(suffix, frameRate, style)
    }

    fun timeRemaining(prefix: String = "eta ", style: TextStyle = DEFAULT_STYLE, frameRate: Int? = 1) {
        cells += EtaProgressCell(prefix, frameRate, style)
    }

    internal fun build(t: Terminal): ProgressTracker {
        val ticker = getTicker(frameRate.takeIf { autoUpdate })
        return ProgressTracker(t, cells, frameRate, historyLength, ticker, padding, timeSource)
    }

    private val cells = mutableListOf<ProgressCell>()
}

fun Terminal.progressTracker(init: ProgressBuilder.() -> Unit): ProgressTracker {
    return ProgressBuilder().apply(init).build(this)
}
