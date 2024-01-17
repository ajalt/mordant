package com.github.ajalt.mordant.samples

import com.github.ajalt.mordant.animation.progress.addTask
import com.github.ajalt.mordant.animation.progress.advance
import com.github.ajalt.mordant.animation.progress.animateOnThread
import com.github.ajalt.mordant.animation.progress.execute
import com.github.ajalt.mordant.rendering.TextColors.brightBlue
import com.github.ajalt.mordant.rendering.TextColors.magenta
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Spinner
import com.github.ajalt.mordant.widgets.progress.*

fun main() {
    val terminal = Terminal()

    val progress = progressBarLayout {
        spinner(Spinner.Dots(brightBlue))
        marquee(terminal.theme.warning("my-file-download.bin"), width = 15)
        percentage()
        progressBar()
        completed(style = terminal.theme.success)
        speed("B/s", style = terminal.theme.info)
        timeRemaining(style = magenta)
    }.animateOnThread(terminal)
    val task = progress.addTask()

    progress.execute()

    // Sleep for a few seconds to show the indeterminate state
    Thread.sleep(5000)

    // Update the progress as the download progresses
    task.update { total = 3_000_000_000 }
    while (!progress.finished) {
        task.advance(15_000_000)
        Thread.sleep(100)
    }
}
