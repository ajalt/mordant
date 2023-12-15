package com.github.ajalt.mordant.samples

import com.github.ajalt.mordant.animation.progress.addTask
import com.github.ajalt.mordant.animation.progress.advance
import com.github.ajalt.mordant.animation.progress.animateOnThread
import com.github.ajalt.mordant.animation.progress.execute
import com.github.ajalt.mordant.rendering.TextColors.brightBlue
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.*
import com.github.ajalt.mordant.widgets.progress.*

fun main() {
    val terminal = Terminal()

    val progress = progressBarLayout {
        spinner(Spinner.Dots(brightBlue))
        text("my-file.bin")
        percentage()
        progressBar()
        completed()
        speed("B/s")
        timeRemaining()
    }.animateOnThread(terminal)
    val task = progress.addTask()

    progress.execute()

    // Sleep for a few seconds to show the indeterminate state
    Thread.sleep(5000)

    // Update the progress as the download progresses
    task.update { total = 3_000_000_000 }
    repeat(200) {
        task.advance(15_000_000)
        Thread.sleep(100)
    }
}
