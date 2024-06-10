package com.github.ajalt.mordant.samples

import com.github.ajalt.colormath.Color
import com.github.ajalt.colormath.model.HSL
import com.github.ajalt.colormath.model.Oklab
import com.github.ajalt.colormath.model.RGB
import com.github.ajalt.colormath.transform.interpolator
import com.github.ajalt.mordant.animation.coroutines.animateInCoroutine
import com.github.ajalt.mordant.animation.textAnimation
import com.github.ajalt.mordant.input.*
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun main() = coroutineScope {
    val terminal = Terminal(ansiLevel = AnsiLevel.TRUECOLOR, interactive = true)
    var hue = 0
    val canvas = List(terminal.info.height - 1) {
        MutableList<Color>(terminal.info.width) { RGB("#000") }
    }
    val animation = terminal.textAnimation<Unit> {
        buildString {
            for ((y, row) in canvas.withIndex()) {
                for ((x, color) in row.withIndex()) {
                    append(TextColors.color(color).bg(" "))
                    canvas[y][x] = Oklab.interpolator {
                        stop(color)
                        stop(RGB("#000"))
                    }.interpolate(0.025)
                }
                append("\n")
            }
        }
    }.animateInCoroutine()

    launch { animation.execute() }

    terminal.receiveEvents(MouseTracking.Button) { event ->
        when (event) {
            is KeyboardEvent -> when {
                event.isCtrlC -> InputReceiver.Status.Finished
                else -> InputReceiver.Status.Continue
            }

            is MouseEvent -> {
                if (event.left) {
                    canvas[event.y][event.x] = HSL(hue.toDouble(), 1, .5)
                    hue += 2
                }
                InputReceiver.Status.Continue
            }
        }
    }

    animation.clear()
}
