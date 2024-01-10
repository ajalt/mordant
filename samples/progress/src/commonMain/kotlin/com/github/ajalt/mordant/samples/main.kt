package com.github.ajalt.mordant.samples

import com.github.ajalt.mordant.animation.progressAnimation
import com.github.ajalt.mordant.rendering.TextColors.brightBlue
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Spinner
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

suspend fun main() {
    val terminal = Terminal()

    val progress = terminal.progressAnimation {
        spinner(Spinner.Dots(brightBlue))
        text("my-file.bin")
        percentage()
        progressBar()
        completed()
        speed("B/s")
        timeRemaining()
    }

    progress.start()

    // Sleep for a few seconds to show the indeterminate state
    delay(5.seconds)

    // Update the progress as the download progresses
    progress.updateTotal(3_000_000_000)
    repeat(200) {
        progress.advance(15_000_000)
        delay(0.1.seconds)
    }

    progress.stop()
}
