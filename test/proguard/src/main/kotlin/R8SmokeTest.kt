package com.github.ajalt.mordant.main

import com.github.ajalt.mordant.animation.progress.animateOnThread
import com.github.ajalt.mordant.animation.progress.execute
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.progress.progressBar
import com.github.ajalt.mordant.widgets.progress.progressBarLayout

fun main(args: Array<String>) {
    // make sure that the terminal detection doesn't crash.
    Terminal()

    // make sure animations and markdown don't crash.
    val t = Terminal(interactive = true, ansiLevel = AnsiLevel.TRUECOLOR)
    val animation = progressBarLayout { progressBar() }.animateOnThread(t, total = 1)
    animation.execute()
    t.print(Markdown("- Your args: **${args.asList()}**"))
    Thread.sleep(100)
    animation.update { completed = 1 }
}
