package com.github.ajalt.mordant.main

import com.github.ajalt.mordant.animation.progressAnimation
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal

suspend fun main(args: Array<String>) {
    // make sure that the terminal detection doesn't crash.
    Terminal()

    // make sure animations and markdown don't crash.
    val t = Terminal(interactive = true, ansiLevel = AnsiLevel.TRUECOLOR)
    val animation = t.progressAnimation { progressBar() }
    animation.start()
    t.print(Markdown("- Your args: **${args.asList()}**"))
    Thread.sleep(100)
    animation.clear()
}
