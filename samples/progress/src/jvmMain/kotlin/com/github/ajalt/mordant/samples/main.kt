package com.github.ajalt.mordant.samples

import com.github.ajalt.mordant.animation.coroutines.animateInCoroutine
import com.github.ajalt.mordant.animation.progress.MultiProgressBarAnimation
import com.github.ajalt.mordant.animation.progress.advance
import com.github.ajalt.mordant.animation.progress.removeTask
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors.brightBlue
import com.github.ajalt.mordant.rendering.TextColors.magenta
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Spinner
import com.github.ajalt.mordant.widgets.progress.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

suspend fun main() = coroutineScope {
    val terminal = Terminal()
    val modules = listOf(
        ":mordant",
        ":extensions:mordant-coroutines",
        ":extensions:mordant-native",
        ":samples:progress",
        ":samples:tables",
        ":samples:widgets",
        ":samples:terminal-themes",
        ":samples:markdown",
    )
    val overallLayout = progressBarContextLayout(alignColumns = false) {
        progressBar(width = 20)
        percentage()
        text { (terminal.theme.success + bold)(context) }
        timeElapsed(compact = false)
    }
    val taskLayout = progressBarContextLayout<String> {
        text(fps = animationFps, align = TextAlign.LEFT) { "〉$context" }
    }

    val progress = MultiProgressBarAnimation<String>(terminal).animateInCoroutine(terminal)
    val overall = progress.addTask(overallLayout, "INITIALIZING", total = 100)
    launch { progress.execute() }
    val task1 = progress.addTask(taskLayout, bold("Evaluate settings"))
    delay(200)


    overall.update { context = "CONFIGURING" }
    task1.update { context = "Resolve dependencies for buildSrc" }
    val task2 = progress.addTask(taskLayout, dim("IDLE"))
    val task3 = progress.addTask(taskLayout, dim("IDLE"))
    val tasks = listOf(task1, task2, task3)
    delay(200)

    overall.update { context = "EXECUTING" }
    repeat(5) {
        for (module in modules) {
            tasks[Random.nextInt(tasks.size)].update { context = module }
            overall.advance()
            delay(100)
        }
    }

    overall.update { context = "EXECUTING" }
    tasks.forEach { progress.removeTask(it) }

    val dlLayout = progressBarContextLayout {
        spinner(Spinner.Dots(brightBlue))
        marquee(width = 15) { terminal.theme.warning(context) }
        percentage()
        progressBar()
        completed(style = terminal.theme.success)
        speed("B/s", style = terminal.theme.info)
        timeRemaining(style = magenta)
    }

    val download1 = progress.addTask(dlLayout, "ubuntu-desktop-amd64.iso", total = 3_000_000_000)
    val download2 = progress.addTask(dlLayout, "fedora-kde-live-x86_64.iso", total = 2_500_000_000)
    val download3 = progress.addTask(dlLayout, "archlinux-x86_64.iso")
    val downloads = listOf(download1, download2, download3)
    while (!progress.finished) {
        if (!download1.finished) download1.advance(15_000_000)
        if (!download2.finished) download2.advance(10_000_000)
        if (download1.completed > 1_000_000_000) {
            if (!download3.finished) download3.update {
                total = 1_000_000_000
                completed += 6_000_000
            }
        }
        overall.update { completed = 40 + 20 * downloads.count { it.finished }.toLong() }
        delay(50)
    }
}
