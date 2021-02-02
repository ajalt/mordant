package com.github.ajalt.mordant.samples

import com.github.ajalt.mordant.animation.progressAnimation
import com.github.ajalt.mordant.terminal.Terminal

fun main() {
    val terminal = Terminal()

    // Detect the terminal size so our progress bar is as wide as the screen
    terminal.info.updateTerminalSize()

    val progress = terminal.progressAnimation {
        text("my-file.bin")
        percentage()
        progressBar()
        completed()
        speed("B/s")
        timeRemaining()
    }

    progress.start()

    // Sleep for a few seconds to show the indeterminate state
    Thread.sleep(5000)

    // Update the progress as the download progresses
    progress.updateTotal(3_000_000_000)
    repeat(200) {
        progress.advance(15_000_000)
        Thread.sleep(100)
    }

    progress.stop()
}
