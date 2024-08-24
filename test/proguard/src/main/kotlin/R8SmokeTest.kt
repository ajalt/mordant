package com.github.ajalt.mordant.main

import com.github.ajalt.mordant.animation.coroutines.animateInCoroutine
import com.github.ajalt.mordant.animation.progress.animateOnThread
import com.github.ajalt.mordant.animation.progress.execute
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.progress.progressBar
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

suspend fun main(args: Array<String>) = coroutineScope{
    // make sure that the terminal detection doesn't crash.
    Terminal()

    // make sure animations and markdown don't crash.
    val t = Terminal(interactive = true, ansiLevel = AnsiLevel.TRUECOLOR)
    val animation = progressBarLayout { progressBar() }.animateInCoroutine(t, total = 1)
    launch { animation.execute() }
    t.print(Markdown("- Your args: **${args.asList()}**"))
    delay(100)
    animation.update { completed = 1 }
}
